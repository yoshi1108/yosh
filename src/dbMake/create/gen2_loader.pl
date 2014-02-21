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

my ($TABLE_ID, $FILE) = @ARGV;

my $input_dir = "$BIN_PATH/out/create";

sub copyTable {
    my ($table_id, $file) = @_;
    my $table_name = Com::getTableName($table_id, "raw");
    if (!$table_name) {
        $table_name = Com::getTableName($table_id, "proc");
    }

    $file = File::Spec->rel2abs($file);
    my $sql_out = Com::dbAccess("COPY $table_name FROM '$file' WITH CSV;");
}

copyTable ( $TABLE_ID, $FILE ) ;

