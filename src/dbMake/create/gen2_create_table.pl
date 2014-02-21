#!/usr/bin/perl
package Main;
use File::Spec;
use strict;              # 変数宣言ミス等を厳格にチェック
use warnings;            # 警告を出力
use bigint;              # intを64bitで計算
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視
require "./gen2_com.pl";

my $BIN_PATH = File::Spec->rel2abs($0);    # スクリプトの絶対パス取得
my $BIN_NAME = $BIN_PATH;
$BIN_NAME =~ s/^.*\///g;
$BIN_PATH =~ s/\/[^\/]*$//g;
Internals::SvREADONLY( $BIN_PATH, 1 );
Internals::SvREADONLY( $BIN_NAME, 1 );

my ($MODE) = @ARGV;

# リンケージのSQL
#

my $input_dir = "$BIN_PATH/out/create";

foreach my $file (`ls $input_dir/*.sql`) {
    chomp $file;
    my $table_id = $file;
    $table_id =~ s/\.sql//g;
    $table_id =~ s/^.*\///g;
    my $table_name = `grep "CREATE TABLE" $file`;
    chomp $table_name;
    $table_name =~ s/CREATE TABLE //g;
    $table_name =~ s/ .*$//g;
    printf( "%-40s, %-40s\n", $table_id, $table_name );

    # テーブル名の１６進数のやつ
    my $model_id = $table_name; $model_id =~ s/^.*_//g;
    $model_id = hex($model_id);
    $model_id += 0;

    my $service_id = Com::dbSelect("SELECT service_ap_id from service_ap WHERE service_ap_name='$Com::SERVICE_AP';");
    $service_id =~ s/[ \r\n]//g ;

    if ( defined $MODE && $MODE eq "DROP" ) {
        # シーケンス
        my $sql_out = Com::dbAccess("DROP TABLE $table_name;");
        $sql_out = Com::dbAccess("DROP SEQUENCE ${table_name}_seq;");
        # テーブル定義
        if ( $table_name =~ /^wf_proc.*$/ ) {
            $sql_out = Com::dbAccess( "DELETE FROM wf_data_model WHERE wf_data_model_id = $model_id AND service_ap_id = $service_id ;");
        } else {
            $sql_out = Com::dbAccess( "DELETE FROM dev_template WHERE dev_template_id = $model_id AND service_ap_id = $service_id;");
            $sql_out = Com::dbAccess( "DELETE FROM dev_data_model WHERE dev_data_model_name = '$table_id' AND dev_template_id = $model_id;");
        }
    } else {
         my $sql_out;
        # シーケンス作成
        my $sql_out = Com::dbAccess("DROP SEQUENCE ${table_name}_seq;");
        $sql_out = Com::dbAccess("CREATE SEQUENCE ${table_name}_seq START WITH 1 INCREMENT BY 1 NO MINVALUE MAXVALUE 4294967295 CACHE 1 CYCLE;");
        $sql_out = Com::dbAccess("DROP TABLE $table_name");

        # テーブル定義生成
        $sql_out = Com::dbAccess(`cat $file`);
        $sql_out = Com::dbAccess("ALTER TABLE  ${table_name}_seq OWNER TO postgres;");
        if ( $table_name =~ /^wf_proc.*$/ ) {
            $sql_out = Com::dbAccess( "INSERT INTO wf_data_model(wf_data_model_id, wf_data_model_name, service_ap_id) 
                             VALUES ($model_id, '$table_id', $service_id)");
        } else {
            $sql_out = Com::dbAccess( "INSERT INTO dev_template(dev_template_id, dev_template_name, service_ap_id, dev_protocol_id, status)
                             VALUES ($model_id, '$table_id', $service_id, 1, 1)");
            $sql_out = Com::dbAccess( "INSERT INTO dev_data_model(dev_data_model_name, dev_template_id, dev_data_model_id, dev_message_id, authorization_exclusion_flag)
                             VALUES ('$table_id', $model_id, $model_id, 1, 'T')");
        }
    }
}
