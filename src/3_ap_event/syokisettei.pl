#!/usr/bin/perl
################################
#
# 初期値設定 スクリプト
#
################################
use strict;                 # 変数宣言ミス等を厳格にチェック
use warnings;               # 警告を出力

# DB設定
my $HOSTNAME="172.19.162.129";
my $USER="m2mpf";
my $PASSWORD="t4(Xek5D";
my $PORT="49155";
my $DATABASE="m2m_pf";

# 引数チェック
#if (@ARGV != 1){
#    print "  引数に「対象コントローラID」を指定
#  例）# perl syokisettei.pl 999-0000006700 \n";
#    exit;
#}
# 検索対象のctrl_id
#my $ctrl_id = $ARGV[0];  # 取得対象コントローラ

# 初期値ファイル
my $file = "wf_proc_data_id.txt";


################################
# 前回値
################################
# wf_proc_data_id取得
my $pre_wf_proc = "";   # wf_proc_data_id値
open(my $fh, "<", $file) or die "Cannot open $file: $!";
while (my $line = readline $fh){
  chomp($line);
  $pre_wf_proc = $line;
}
if ($pre_wf_proc eq '') {
    print "前回 wf_proc_data_id が存在しません\n";
    exit;
}
print "修正前wf_proc_data_id の値 : " . $pre_wf_proc . "\n";


################################
# UPDATE 実行
################################
#wf_proc_status のUPDATE
my $sql_up     = "UPDATE wf_proc_data_52dc906000000002 SET wf_proc_status = 0;";
print $sql_up;

my $db_access_up = dbAccess($sql_up);
print "\n";
print $db_access_up;
print "\n";
#my $wf_proc_data_id_15 = "";
#if ($db_access_15 ne ""){
#  $wf_proc_data_id_15 = substr($db_access_15, 1, 16);
#}

################################
# 最初の1件目を取得
################################
#最初の1件目を取得
my $sql_get_1  = "SELECT wf_proc_data_id FROM wf_proc_data_52dc906000000002 WHERE wf_proc_status = 0 ORDER BY wf_proc_data_id DESC LIMIT 1;";
print $sql_get_1;
print "\n";

my $db_access_1 = dbAccess($sql_get_1);
print "DB取得結果 : $db_access_1\n";
my $wf_proc_data_id = $db_access_1 - 500;
print " 今回設定するwf_proc_data_id : $wf_proc_data_id\n";

################################
# 確認用SQL作成
################################
my $sql_assert = "SELECT COUNT(*) FROM wf_proc_data_52dc906000000002 WHERE wf_proc_data_id > $wf_proc_data_id;";
my $db_access_assert = dbAccess($sql_assert);
if ( $db_access_assert != 500) {
    print "失敗  db_access_assert : $db_access_assert\n";
    exit;
}
print "DB取得結果 : $db_access_assert\n";


################################
# 値再設定
################################
if ($wf_proc_data_id ne "") {
  `cp -p $file $file.autobk `;

  open(my $fh, ">", $file) or die "Cannot open $file: $!";
  print $fh $wf_proc_data_id;
  close($fh);
}
print "正常終了 \n";


####################################
# DBアクセス関数
####################################
sub dbAccess {
    my ( $SQL_STR ) = @_;

    # psqlが読み込むパスワード環境変数の設定
    if ( !defined $PASSWORD || $PASSWORD eq "" ) {
        $PASSWORD = "NOPASS";    # 何か設定しておかないと、パスワード有り環境ではpsqlが固まる
    }
    $ENV{'PGPASSWORD'} = $PASSWORD;

    # postgre コマンドライン
    my $PSQL = "psql -h $HOSTNAME -p $PORT -U $USER $DATABASE ";

    $SQL_STR =~ s/[\n\r]//g;
    my $PSQL_OUT = `echo "$SQL_STR" | ${PSQL} -t`;
    if ( $? != 0 ) {
        print ( $PSQL_OUT );
    }
    chomp $PSQL_OUT;
    return $PSQL_OUT;
}
