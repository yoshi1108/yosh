#!/usr/bin/perl
############################
#
# ポーリング起動スクリプト
#
############################
use strict;                 # 変数宣言ミス等を厳格にチェック 
use warnings;               # 警告を出力

my $JMETER_DIR = "../bin"
my $JMETER_SH  = "jmeter.sh";

my $JMX_DIR  = ".";
my $JMX_FILE = "sekisan.jmx";

my $LOG_DIR  = "./logs";
my $LOD_FILE = "sekisan.log";

# 引数チェック
if (@ARGV != 4){
    print "  引数に「コントローラ数」「1デバイス辺りのスレッド数」「datetime」「遅延時間」を指定
  例）# perl 2_sekisan.pl 1000 5 900 '2012-02-02 13:00:00+0900' 10\n";
    exit;
}

my $cont_num   = $ARGV[0];   # コントローラ数
my $thread_num = $ARGV[1];   # スレッド数
my $datetime   = $ARGV[2];   # datetime (body部にinjectionされる)
my $delay      = $ARGV[3];   # 遅延時間

my $command = "$JMETER_DIR/$JMETER_SH -n -Jcont_num=$cont_num -Jthread_num=$thread_num -Jdatetime=\'$datetime\' -Jdelay=$delay -t $JMX_DIR/$JMX_FILE -l $LOG_DIR/$LOD_FILE";
print "$command\n";

## 実行
printf ("\x33b[0m");
system ($command);
printf ("\x1b[0m");
