#!/usr/bin/perl

use strict;                 # 変数宣言ミス等を厳格にチェック 
use warnings;               # 警告を出力
require "./util.pl";

############################
#
# ポーリング起動スクリプト
#
############################

my $JMETER_DIR = "../bin"
my $JMETER_SH  = "jmeter.sh";

my $JMX_DIR  = ".";
my $JMX_FILE = "polling.jmx";

my $LOG_DIR  = "./logs";
my $LOD_FILE = "polling.log";

# 引数チェック
if (@ARGV != 3){
    print "  引数に「コントローラ数」「スレッド数」「送信時間」を指定
  例）# perl 3_polling.pl 1000 20 900\n";
    exit;
}

my $cont_num = $ARGV[0];  #コントローラ数
my $thread_num = $ARGV[1];  #スレッド数
my $test_time = $ARGV[2];  #テスト時間

my $command = "$JMETER_DIR/$JMETER_SH -n -Jcont_num=$cont_num -Jthread_num=$thread_num -Jtest_time=$test_time -t $JMX_DIR/$JMX_FILE -l $LOG_DIR/$LOD_FILE";
print "$command\n";

### 実行
util::pColor(system ($command), "GREEN", 1);
	
