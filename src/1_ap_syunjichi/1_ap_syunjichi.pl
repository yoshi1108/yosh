#!/usr/bin/perl
############################
#
# 瞬時値APL起動スクリプト
#
############################
use strict;                 # 変数宣言ミス等を厳格にチェック
use warnings;               # 警告を出力

my $JMETER_DIR = "/var/m2m_install/an/tool/apache-jmeter-2.11/bin";
my $JMETER_SH  = "jmeter.sh";

my $JMX_DIR  = "/var/m2m_install/an/tool/apache-jmeter-2.11/gen2_apl/1_ap_syunjichi";
my $JMX_FILE = "1_ap_syunjichi.jmx";

my $LOG_DIR  = "/var/m2m_install/an/tool/apache-jmeter-2.11/gen2_apl/1_ap_syunjichi/logs";
my $LOD_FILE = "1_ap_syunjichi.log";

# 引数チェック
if (@ARGV != 2){
    print "  引数に「総コントローラ数」「1回に処理するコントローラ数」を指定
  例）# perl 1_ap_syunjichi.pl 5000 100 \n";
    exit;
}

my $cont_num   = $ARGV[0];    # 総コントローラ数
my $loop_repeat = $ARGV[1];   # 1回に処理するコントローラ数

my $command = "$JMETER_DIR/$JMETER_SH -n -Jcont_num=$cont_num -Jloop_repeat=$loop_repeat -t $JMX_DIR/$JMX_FILE -l $LOG_DIR/$LOD_FILE";
print "$command\n";

## 実行
system ($command);
