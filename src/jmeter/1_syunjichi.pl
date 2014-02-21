#!/usr/bin/perl
############################
#
# ポーリング起動スクリプト
#
############################
use strict;                 # 変数宣言ミス等を厳格にチェック 
use warnings;               # 警告を出力

my $JMETER_DIR = "../bin";
my $JMETER_SH  = "jmeter.sh";

my $JMX_DIR  = ".";
my $JMX_FILE = "syunjichi.jmx";

my $LOG_DIR  = "./logs";
my $LOD_FILE = "syunjichi.log";

# 引数チェック
if (@ARGV != 4){
    print "  引数に「コントローラ数」「1デバイス辺りのスレッド数」「テスト時間」「datetime」を指定
  例）# perl 1_syunjichi.pl 1000 4 900 '2012-02-02 13:00:00+0900'\n";
    exit;
}

my $cont_num   = $ARGV[0];   # コントローラ数
my $thread_num = $ARGV[1];   # スレッド数
my $test_time  = $ARGV[2];   # テスト時間
my $datetime   = $ARGV[3];   # datetime (body部にinjectionされる)

my $command = "$JMETER_DIR/$JMETER_SH -n -Jcont_num=$cont_num -Jthread_num=$thread_num -Jtest_time=$test_time -Jdatetime=\'$datetime\' -t $JMX_DIR/$JMX_FILE -l $LOG_DIR/$LOD_FILE";
print "$command\n";

# 実行
system ($command);
