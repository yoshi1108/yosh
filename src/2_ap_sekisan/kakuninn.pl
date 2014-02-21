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
if (@ARGV != 1){
    print "  引数に「対象コントローラID」を指定
  例）# perl kakuninn.pl 999-0000006700 \n";
    exit;
}
# 検索対象のctrl_id
my $ctrl_id = $ARGV[0];  # 取得対象コントローラ 
# 初期値ファイル
my $file_15 = "15_wf_proc_data_id.txt";
my $file_60 = "60_wf_proc_data_id.txt";


################################
# msr_time
################################
# msr_time_15設定
# 現在時間から適切なmsr_time_15を設定する
#my ($sec,$min,$hour,$mday,$mon,$year) = (localtime(time))[0..5];

# 1. 現在のUnixTimeを分のtimeで取得
# 2. 15で割って15かける - 15
# 3. UnixTimeを時間に変換
my $unix_time = time();
# 00,15,30,45 分に正規化し、15~30分前の値を取得する
$unix_time = int ($unix_time / (15 * 60)) * (15 * 60) - (15 * 60);
my ($sec,$min,$hour,$mday,$mon,$year) =  localtime($unix_time);

# msr_time_15の時間
my $msr_time_15 = sprintf( "%04d-%02d-%02d %02d:%02d:%02d+0900", $year + 1900, $mon + 1, $mday, $hour, $min , $sec);
print "          msr_time_15           : $msr_time_15 \n";

# msr_time_60の時間
my $msr_time_60 = sprintf( "%04d-%02d-%02d %02d:%02d:%02d+0900", $year + 1900, $mon + 1, $mday, $hour, 0 , 0);
print "          msr_time_60           : $msr_time_60 \n";

print "\n";

################################
# SQL作成
################################
my $sql_15 = "SELECT wf_proc_data_id , msr_time FROM wf_proc_data_52dc90600000000e WHERE msr_time < \'$msr_time_15\' AND ctrl_id = \'$ctrl_id\' ORDER BY wf_proc_data_id DESC LIMIT 1;";
my $sql_60 = "SELECT wf_proc_data_id , msr_time FROM wf_proc_data_52dc90600000000f WHERE msr_time < \'$msr_time_60\' AND ctrl_id = \'$ctrl_id\' ORDER BY wf_proc_data_id DESC LIMIT 1;";

################################
# DBアクセス
################################
my $db_access_15 = dbAccess($sql_15);
print $sql_15;
print "\n";
print $db_access_15;
my $wf_proc_data_id_15 = "";
if ($db_access_15 ne ""){
  $wf_proc_data_id_15 = substr($db_access_15, 1, 16);
}

my $db_access_60 = dbAccess($sql_60);
print $sql_60;
print "\n";
print $db_access_60;
my $wf_proc_data_id_60 = "";
if ($db_access_60 ne ""){
  $wf_proc_data_id_60 = substr($db_access_60, 1, 16);
}


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
