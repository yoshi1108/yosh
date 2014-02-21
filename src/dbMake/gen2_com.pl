#!/usr/bin/perl
package Com;
use File::Spec;
use strict;              # 変数宣言ミス等を厳格にチェック
use warnings;            # 警告を出力
use bigint;              # intを64bitで計算
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視

my $BIN_PATH = File::Spec->rel2abs($0);    # スクリプトの絶対パス取得
my $BIN_NAME = $BIN_PATH;
$BIN_NAME =~ s/^.*\///g;
$BIN_PATH =~ s/\/[^\/]*$//g;
Internals::SvREADONLY( $BIN_PATH, 1 );
Internals::SvREADONLY( $BIN_NAME, 1 );

my ($MODE) = @ARGV;

# 共通設定値
our $SERVICE_AP="BIZtefH9jINHY3pdo0Q";

# 設定値
our $DB_IP="172.19.162.129";
our $DB_PASSWORD="t4(Xek5D";
our $PSQL="psql -h $DB_IP -p 49155 -U m2mpf m2m_pf ";

####################################
# DBアクセス関数
####################################
# 大量INSERTしたいときよう（リストでたくさんうけつける）
sub dbAccess {
    my @sql_str_list = @_;
    # psqlが読み込むパスワード環境変数の設定
    if ( !defined $DB_PASSWORD || $DB_PASSWORD eq "" ) {
        $DB_PASSWORD = "NOPASS";   # 何か設定しておかないと、パスワード有り環境ではpsqlが固まる
    }
    $ENV{'PGPASSWORD'} = $DB_PASSWORD;
    my $cmd_fh;    # psqlコマンドへの入力ハンドラ
    open( cmd_fh, "| $PSQL -t " );
    foreach my $line (@sql_str_list) {
        print cmd_fh "$line\n";
    }
    eval {close(cmd_fh);}; # クローズ
}

# 大量COPYしたいとき
sub dbCopy {
    my ($table_name, @csv_list) = @_;
    # psqlが読み込むパスワード環境変数の設定
    if ( !defined $DB_PASSWORD || $DB_PASSWORD eq "" ) {
        $DB_PASSWORD = "NOPASS";   # 何か設定しておかないと、パスワード有り環境ではpsqlが固まる
    }
    $ENV{'PGPASSWORD'} = $DB_PASSWORD;
    my $cmd_fh;    # psqlコマンドへの入力ハンドラ
    open( cmd_fh, "| $PSQL -t -c 'COPY $table_name FROM stdin WITH CSV;'" );
    foreach my $line (@csv_list) {
        chomp $line;
        print cmd_fh "$line\n";
    }
    eval {close(cmd_fh);}; # クローズ
}

# SQLの結果が欲しい場合の関数
sub dbSelect {
    my ($sql_str) = @_;

    my ($pkg,$file,$line) = caller();
    # psqlが読み込むパスワード環境変数の設定
    if ( !defined $DB_PASSWORD || $DB_PASSWORD eq "" ) {
        $DB_PASSWORD = "NOPASS";   # 何か設定しておかないと、パスワード有り環境ではpsqlが固まる
    }
    $ENV{'PGPASSWORD'} = $DB_PASSWORD;

    $sql_str =~ s/[\n\r]//g ;
    my $psql_out;
    $psql_out=`echo "$sql_str" | ${PSQL} -t`;
    chomp $psql_out;
    return $psql_out;
}

# テーブル名変換
sub getTableName {
    my ($table_id , $table_kind) = @_;
    my $sql_str;
    if ( $table_kind eq "proc" ) {
        $sql_str = "SELECT 'wf_proc_data_' || to_hex(wf_data_model.wf_data_model_id)
                    FROM wf_data_model LEFT OUTER JOIN service_ap ON wf_data_model.service_ap_id = service_ap.service_ap_id
                    WHERE service_ap_name = '$Com::SERVICE_AP' AND wf_data_model_name = '$table_id' LIMIT 1;";
    }
    if ( $table_kind eq "raw" ) {
        $sql_str = "SELECT 'dev_raw_data_' || to_hex(dev_data_model.dev_data_model_id) FROM dev_data_model
                    LEFT OUTER JOIN dev_template ON dev_data_model.dev_template_id = dev_template.dev_template_id
                    LEFT OUTER JOIN service_ap ON dev_template.service_ap_id = service_ap.service_ap_id
                    WHERE service_ap.service_ap_name = '$Com::SERVICE_AP' and dev_data_model_name = '$table_id' LIMIT 1;";
    }
    $sql_str =~ s/[\r\n]//g ;
    my $sql_out = Com::dbSelect($sql_str);
    $sql_out =~ s/[ \r\n]//g ;

    return $sql_out;
}

#########################
sub printColor { # カラー表示
##########################
    # 黒: 30 赤: 31 緑: 32 黄: 33 青: 34 マゼンタ: 35 シアン: 36 白: 37
    my ($MSG, $COLOR, $MODE) = @_;

    my $COLOR_CODE;
    if    ( $COLOR eq "BLACK" ) { $COLOR_CODE="30"; }
    elsif ( $COLOR eq "RED" ) { $COLOR_CODE="31"; }
    elsif ( $COLOR eq "GREEN" ) { $COLOR_CODE="32"; }
    elsif ( $COLOR eq "YELLOW" ) { $COLOR_CODE="33"; }
    elsif ( $COLOR eq "BLUE" ) { $COLOR_CODE="34"; }
    elsif ( $COLOR eq "PINK" ) { $COLOR_CODE="35"; }
    elsif ( $COLOR eq "CYAN" ) { $COLOR_CODE="36"; }
    elsif ( $COLOR eq "WHITE" ) { $COLOR_CODE="37"; }
    else                       { $COLOR_CODE="37"; }
    printf ("\x1b[${MODE};${COLOR_CODE}m");
    printf ("%s", $MSG);
    printf ("\x1b[0m");
}
1;
