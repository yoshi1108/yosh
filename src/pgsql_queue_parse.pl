#!/usr/bin/perl
use strict;      # 変数宣言ミス等を厳格にチェック
use warnings;    # 警告を出力
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視

use File::Spec;
use Math::BigFloat;
use Time::Local;
use bigint;      # intを64bitで計算
#use threads;
use Time::HiRes qw( usleep gettimeofday tv_interval );

my $BIN_PATH = File::Spec->rel2abs($0);    # スクリプトの絶対パス取得
my $BIN_NAME = $BIN_PATH;
$BIN_NAME =~ s/^.*\///g;
$BIN_PATH =~ s/\/[^\/]*$//g;
Internals::SvREADONLY( $BIN_PATH, 1 );
Internals::SvREADONLY( $BIN_NAME, 1 );

my $QUEUE_STR="device_proxy_queue_normal"; # キューの文字列
#my $QUEUE_STR="device_control_queue_normal"; # キューの文字列

my ($PGSQL_DIR) = @ARGV;         # 引数 例 2014/01/02 10:00:00 2014/01/02 12:00:00
if (!$PGSQL_DIR){
    $PGSQL_DIR="/var/opt/postgresql/postgresql.queue/";
}

my %th_info_map;       # スレッドID毎のハッシュマップ
my $data_num=0;        # データ数合計
my $delta_msec_sum=0;  # 差分ミリ秒合計
my $delta_msec_min=999999;  # データの最小値
my $delta_msec_max=0;  # データ数最大値
# yyyy/mm/dd HH:MM:SSをunixtimeに変換
sub getUnixtime{
    my ($target) = @_;
    my ($date, $time) = split / /, $target;
    my ($yyyy, $mm, $dd) = split /-/, $date;
    my ($HH, $MM, $SS) = split /:/, $time;
    return timelocal( $SS, $MM, $HH, $dd, $mm - 1, $yyyy - 1900 );
}

sub deltaMSec{
    #2014-01-18 05:13:57.883
    my ($start, $end) = @_;
    if (!$start) { return 100;}
    my ($start_date, $start_msec) = split /\./, $start; # yyyy/mm/dd HH:MM:SSとmsecに分解
    my ($end_date, $end_msec) = split /\./, $end;

    my $start_unix_time = getUnixtime($start_date);
    my $end_unix_time   = getUnixtime($end_date);
    # 差分ミリ秒を計算
    my $delta_msec = (($end_unix_time - $start_unix_time) * 1000) + ($end_msec - $start_msec);
    return $delta_msec;
}

#foreach my $file ( sort (`dir /B $PGSQL_DIR`) ) { # DOS
foreach my $file ( sort (`ls $PGSQL_DIR`) ) {
my %old_th_info_map; # 前回同一スレッドID情報
    chomp $file;
    if ( !($file =~ /pgsql.*\.log$/) ) {
        next; # ログファイル以外の場合はスキップ
    }
    print "$PGSQL_DIR/$file\n";
    my $file_fh;
    open( $file_fh, "<", "$PGSQL_DIR/$file" );
    while ( my $line = readline($file_fh) ) {
        chomp $line;
        if ( ! ($line =~ /${QUEUE_STR}/) ) { next ;} # キュー以外ならnext
        my $time=$line; $time =~ s/JST.*$//; # 時刻
        my $th_id=$line; $th_id =~ s/^.*\[//g; $th_id =~ s/\].*$// ; $th_id =~ s/-.*$//; # スレッドID
        my $msg=$line; $msg =~ s/^.*LOG://g; $msg =~ s/$QUEUE_STR.*$/$QUEUE_STR/; # メッセージ
        my $crud;        # SELECT,UPDATE,DELETE,INSERTの種別
        if ( $msg =~ / SELECT / ) {
            $crud = "SELECT";
        } elsif ( $msg =~ / delete / ) {
            $crud = "DELETE";
        } else {
            next; # SELECT, delete以外なら次へ
        }

        if ( $crud eq "SELECT" ) {
            #SELECTの場合はスレッド毎の情報を更新（同一スレッドIDで連続SELECTは一つ前は空振りということ）
            $th_info_map{$th_id}{'time'} = $time;
            $th_info_map{$th_id}{'crud'} = $crud;
            $th_info_map{$th_id}{'msg'} = $msg;
        } else {
            $data_num++; # データ数カウントアップ
            #DELETEの場合は時刻差分計算して統計処理
            my $delta_msec = deltaMSec($old_th_info_map{$th_id}{'time'}, $time);      # 差分秒計算
            if ( $delta_msec < $delta_msec_min ) { $delta_msec_min = $delta_msec; }   # 差分秒最小値更新
            if ( $delta_msec > $delta_msec_max ) { $delta_msec_max = $delta_msec; }; # 差分秒最大値更新
            $delta_msec_sum += $delta_msec;                                           # 差分秒合計値更新
            #if ( $delta_msec  <= 10 ) {  # 異常値(かな？と思った場合の）調査用ログ
            #    printf ("%s,%6d,%6d,%20d,%s\n", $time ,$th_id ,$delta_msec, $delta_msec_sum, $msg);
            #}
        }
        #前回スレッドID情報保持 (要素を１つずつコピー。一括でハッシュマップ内のハッシュマップコピーする方法ないのかな。。。）
        $old_th_info_map{$th_id}{'time'} = $th_info_map{$th_id}{'time'};
        $old_th_info_map{$th_id}{'crud'} = $th_info_map{$th_id}{'crud'};
        $old_th_info_map{$th_id}{'msg'}  = $th_info_map{$th_id}{'msg'};
    }
    eval{ close(file_fh); };
}

printf "DataNum=%d, Sum=%g(msec), Min=%d(msec), Max=%d(msec), Avg:%.3f(msec)\n",
    $data_num, $delta_msec_sum, $delta_msec_min, $delta_msec_max, Math::BigFloat->new($delta_msec_sum)->fdiv($data_num);

#########