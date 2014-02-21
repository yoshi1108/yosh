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

my $PGSQL_DIR="/var/opt/postgresql/postgresql.queue/";

my ($START_DATE, $END_DATE) = @ARGV;         # 引数 例 '2014/01/02 10:00:00' '2014/01/02 12:00:00'

# yyyy/mm/dd HH:MM:SSをunixtimeに変換
sub getUnixtime{
    my ($target) = @_;
    my ($date, $time) = split / /, $target;
    my ($yyyy, $mm, $dd) = split /-/, $date;
    my ($HH, $MM, $SS) = split /:/, $time;
    return timelocal( $SS, $MM, $HH, $dd, $mm - 1, $yyyy - 1900 );
}

my $start_str = $START_DATE;  $start_str =~ s/[\/ :]//g ;
my $end_str = $END_DATE;  $end_str =~ s/[\/ :]//g ;

my $BAK_DIR="$PGSQL_DIR/$start_str";
`mkdir -p $BAK_DIR`;

foreach my $file ( sort (`ls $PGSQL_DIR`) ) {
    chomp $file;
    if ( !($file =~ /pgsql.*\.log$/) ) {
        next; # ログファイル以外の場合はスキップ
    }
    # pgsql_2014/01/22 03:23:56.log
    my $log_time = $file; $log_time =~ s/^.*_// ; $log_time =~ s/\.log// ;

    if ( $log_time < $start_str ) {
        next; # 開始時刻前のファイル
    }

    `cp -p $PGSQL_DIR/$file $BAK_DIR`;

    if ( $log_time > $end_str ) {
        last;
    }
    print "$log_time , $file\n";
}
#########