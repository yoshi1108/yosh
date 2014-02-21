#!/usr/bin/perl
package Main;
use strict;      # 変数宣言ミス等を厳格にチェック
use warnings;    # 警告を出力
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視

use File::Spec;
#use Math::BigFloat;
use Time::Local;
use bigint;      # intを64bitで計算
use threads;
use Time::HiRes qw( usleep ualarm gettimeofday tv_interval );

require "./gen2_com.pl";
require "./gen2_make_data_conf.pl";

my $BIN_PATH = File::Spec->rel2abs($0);    # スクリプトの絶対パス取得
my $BIN_NAME = $BIN_PATH;
$BIN_NAME =~ s/^.*\///g;
$BIN_PATH =~ s/\/[^\/]*$//g;
Internals::SvREADONLY( $BIN_PATH, 1 );
Internals::SvREADONLY( $BIN_NAME, 1 );

# ログ
my $tmp_date=`date "+%Y_%m_%d_%H_%M"`;
chomp $tmp_date;
my $LOG_F="dbMake_" . $tmp_date . ".log";
`date > $LOG_F`;
for my $tmp_v (@ARGV) {
    `echo $tmp_v >> $LOG_F`;
}

my @threads;                               # スレッドリスト

# 変数設定
my $ONE_COPY_MAX = 10000;                  # 一回のCOPY文に渡すデータ数
#######################################
#my ($END_DATE, $END_TIME, $CTRL_NUM) = @ARGV;     # 引数で終了日付、終了時刻を指定 例 2014/01/02 03:04:05 5000
my ($END, $CTRL_NUM) = @ARGV;     # 引数で終了日付、終了時刻を指定 例 2014/01/02 03:04:05

### 引数チェック
if (@ARGV != 2){
     print "  引数に「データ終了時刻」「コントローラ数」を指定\n";
     print "例）#perl gen2_make_data.pl '2014/01/30 10:00:00' 1000 \n";
     exit;
}

my $input_dir = "$BIN_PATH/out/create";
my ($END_DATE, $END_TIME) = split / /, $END;

print "$END_DATE $END_TIME\n";

#################
# 雛形CSVデータから可変データを生成
#################
sub makeCsvData {
    my ( $data_id, $registered_time, $serial_id, $data_temp ) = @_;
    my $data = $data_temp;

    $data =~ s/\$D\$/$data_id/;
    $data =~ s/\$R\$/$registered_time/;
    $data =~ s/\$B\$/$data_id/;
    $data =~ s/\$I\$/1/;
    $data =~ s/\$S\$/$serial_id/;

    return $data;
}

##########################################
# 雛形データ生成(高速置換ようにプリプロセス)
##########################################
sub makeTempData {
    my ($org_data, $table_kind) = @_;
    chomp $org_data;
    my $data = $org_data;
    #01 $D$ data_id         : 4294967320,
    #02 $R$ registered_time : 2014-01-16 15:35:31.267+09,
    #03     acceptid        : 10.27.186.153--1b42b9e5:1438efe2c3d:-76d4, 固定
    #04 $B$ sub_accept_id   : 0
    #05 $I$ dev_instance_id : 5968967212348741432,
    #06 $S$ serial_id       : 001-NESREG0011-001-0000001,
    #07 ,,2014-01-16 11:30:00+09,888,30,30,40,40,0002,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,  # 入力ファイルデータのまま
    # 高速に置換するため、短い置き換え用の文字に変換してテンプレートとして持っておく
    if ( $table_kind eq "raw" ) {
        $data =~ s/^[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,/\$D\$,\$R\$,10.27.186.153--1b42b9e5:1438efe2c3d:-76d4,\$B\$,5968967212348741432,\$S\$,/;
    }
    #01 wf_proc_data_id      | bigint                   | not null default ((get_localip_num_func() << 32) + nextval('wf_proc_data_52cfa02a000005c2_seq'::regclass))
    #02 wf_definition_id     | bigint                   |
    #03 registered_time      | timestamp with time zone | not null default now()
    if ( $table_kind eq "proc" ) {
        $data =~ s/^[^,]*,[^,]*,[^,]*,/\$D\$,1,\$R\$,/;
    }
    
    return $data;
}

#####################
# COPYするスレッド
#####################
sub copyThread {
    my ( $table_id, $table_name, $cnt, $data_num, @csv_list ) = @_;
    print "copy start $table_id = $table_name cnt=$cnt/$data_num\n";
    my $sql_out = Com::dbCopy( $table_name, @csv_list );
    if ( $? != 0 ) {
        Com::printColor( "load error.\n", "RED", 1 );
        exit 1;
    }
    print "copy end   $table_id = $table_name cnt=$cnt/$data_num\n";
}

#################################
# COPYスレッドを開始させる関数
#################################
sub copyStart {
    my ( $table_id, $table_name, $cnt, $data_num, @list ) = @_;
    my $thread = threads->create( \&copyThread, $table_id, $table_name, $cnt, $data_num, @list );
    $thread->join(); # ここでjoinしたら同期化される
    push( @threads, $thread );
}

sub getDataStrFromUnixTime {
    my ( $unix_time ) = @_;
    my ( $sec, $min, $hour, $day, $mon, $year, $wday, $yday, $isdst ) = localtime($unix_time); $year += 1900; $mon  += 1;
    # 登録日付
    return sprintf( "%04s-%02s-%02s %02s:%02s:%02s+9", $year, $mon, $day, $hour, $min, $sec );
}

# 開始時刻と終了時刻のUnixTimeを生成
sub makeStartEnd {
    my ( $all_sec ) = @_;
    my $start_unix_time;                                             # 開始時刻のunix loaltime
    my $end_unix_time;                                               # 終了時刻のunix localtime
    {
        my ( $year, $mon, $day ) = split( /\//, $END_DATE );         # 引数の日付を分解
        my ( $hour, $min, $sec ) = split( /\:/, $END_TIME );         # 引数の時刻を分解
        $end_unix_time = timelocal( $sec, $min, $hour, $day, $mon - 1, $year - 1900 );    # 終了時刻のunix localtime
        $start_unix_time = $end_unix_time - $all_sec;                                     # 開始時刻のunix loaltime
        printf( "Start Time: %s %s ",  getDataStrFromUnixTime($start_unix_time), $start_unix_time );
        printf( "End   Time: %s %s\n", getDataStrFromUnixTime($end_unix_time),   $end_unix_time );
    }
    return ($start_unix_time , $end_unix_time);
}

##############################################
# 1種類のテーブルIDのデータを生成する関数
##############################################
sub oneTableIdData {
    my ( $table_id, $table_kind, $term, $data_num, $ctrl_num, $serial_id_pt, $file, $end_time ) = @_;

    my $data_temp     = makeTempData( `cat $file`, $table_kind );    # １行データのテンプレート文字列
    my $DATA_ID_START = ( 1 << 32 ) + 1;                             # 1 <<32はm2mのget_localip_num_func()が返却しうる値の最小を32bitシフト、+1 はSEQ開始番号
    my $commit_cnt    = 0;                                           # x件毎にコミット
    my $data_id       = $DATA_ID_START;                              # データID
    my @csv_list      = ();                                          # copy関数に渡す一回のCOPY文発行で使うCSVのリスト
    my $table_name = Com::getTableName( $table_id, $table_kind );    # 物理テーブル名取得
    Com::dbAccess("TRUNCATE $table_name;");                          # テーブルデータ初期化
    my $all_sec = $term * 60 * 60;                                   # データの全体の秒数。時間を秒に変換する
    my ($start_unix_time, $end_unix_time) = makeStartEnd($all_sec);  # 開始、終了時刻のUnixTimeを生成
    my $loop_cnt     = 0;                                             # ループカウンタ
    my $cal_sec_cnt = 0;                                             # 再計算カウンタ
    
    # データ生成、COPYループ
    for ( ; $loop_cnt < $data_num; $loop_cnt++ ) {

        # 登録日付再計算。これを毎回行わないように頑張っても、20%くらいしか早くならない。
        # 関数呼び出しでなくベタに書いて速度かせぐ
        my $elapsed_unix_time = ( $all_sec * $loop_cnt ) / $data_num;
        my $current_unix_time = $start_unix_time + $elapsed_unix_time;    # 今計算しているunix時間
        my ( $sec, $min, $hour, $day, $mon, $year, $wday, $yday, $isdst ) = localtime($current_unix_time);
        $year += 1900;
        $mon  += 1;

        # 登録日付
        my $registered_time = sprintf( "%04s-%02s-%02s %02s:%02s:%02s", $year, $mon, $day, $hour, $min, $sec );

        $commit_cnt++;

        # シリアルID生成
        my $serial_id_cnt = $loop_cnt % $ctrl_num; # コントローラ数で割り算の余り
        $serial_id_cnt = sprintf ("%05d", $serial_id_cnt);
        my $serial_id = $serial_id_pt; $serial_id =~ s/\$CNT\$/$serial_id_cnt/;

        my $data = makeCsvData( $data_id++ , $registered_time, $serial_id, $data_temp );    # CSVデータ生成
#print "$data\n";

        push( @csv_list, $data );                                                    # COPYリストに１行追加
        if ( $commit_cnt > $ONE_COPY_MAX ) {

            # COPYリストが規定値に達したらCOPY処理を行う
            $commit_cnt = 0;                                                         # COPYのコミットカウンタクリア
            my @tmp_list = ();                                                       # copyスレッドに渡すようのリスト
            push( @tmp_list, @csv_list );                                            # copyスレッドに渡すようのリストにコピー
            copyStart( $table_id, $table_name, $loop_cnt, $data_num, @tmp_list );    # COPY開始
            @csv_list = ();                                                          # CSVリストクリア
        }
    }
    # 端数リストを処理
    copyStart( $table_id, $table_name, $loop_cnt, $data_num, @csv_list );

    # スレッド終了待ち
    foreach (@threads) {
        eval { my ($return) = $_->join; };
    }
    Com::printColor( sprintf( "%-20s, %-40s, %s, load \n", $table_id, $table_name, $loop_cnt ), "GREEN", 1 );
}

foreach my $table_id_info ( @DataConf::table_id_list ) {
   # printf ("%-30s, %4s, %10s %s\n", $table_id_info->[1], $table_id_info->[2], $table_id_info->[3], $table_id_info->[0]);
    my $table_kind   = $table_id_info->[0]; # 生：raw 加工:proc 
    my $table_id     = $table_id_info->[2]; # テーブル名（論理）
    my $term         = $table_id_info->[3]; # 収録期間
    my $num1000      = $table_id_info->[4]; # 1000デバイス辺りの件数
    my $serial_id_pt = $table_id_info->[5]; # シリアルIDパターン
    my $file = "out/$table_id.csv";

    my $data_num = ($num1000 * $CTRL_NUM) / 1000;

    oneTableIdData($table_id, $table_kind, $term, $data_num, $CTRL_NUM, $serial_id_pt, $file, $END_TIME);
}

#
`date >> $LOG_F`;

Com::printColor( "load done.\n", "GREEN", 1 );
