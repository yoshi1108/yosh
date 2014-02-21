#!/usr/bin/perl
############################
#
# イベントAPL起動スクリプト
#
############################
use strict;                 # 変数宣言ミス等を厳格にチェック
use warnings;               # 警告を出力

my $JMETER_DIR = "/var/m2m_install/an/tool/apache-jmeter-2.11/bin";
my $JMETER_SH  = "jmeter.sh";

my $JMX_DIR  = "/var/m2m_install/an/tool/apache-jmeter-2.11/gen2_apl/3_ap_event";
my $JMX_FILE = "3_ap_event.jmx";

my $LOG_DIR  = "/var/m2m_install/an/tool/apache-jmeter-2.11/gen2_apl/3_ap_event/logs";
my $LOD_FILE = "3_ap_event.log";


# ファイル読み込み
my $file = "$JMX_DIR/wf_proc_data_id.txt";
my $pre_wf_proc = "";   # wf_proc_data_id値
open(my $fh, "<", $file) or die "Cannot open $file: $!";
while (my $line = readline $fh){
  chomp($line);
  $pre_wf_proc = $line;
}

# 取得判定
if ($pre_wf_proc eq '') {
    print "wf_proc_data_id が存在しません\n";
    exit;
} 

print "前回wf_proc_data_id の値 : " . $pre_wf_proc . "\n";

my $command = "$JMETER_DIR/$JMETER_SH -n -Jpre_wf_proc=$pre_wf_proc -t $JMX_DIR/$JMX_FILE -l $LOG_DIR/$LOD_FILE";
print "$command\n";

## 実行
system ($command);
