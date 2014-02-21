#!/usr/bin/perl
package Main;
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

# 設定値
my $SERVICE_AP="BIZtefH9jINHY3pdo0Q";
my $PSQL="psql -h 192.168.233.2 -p 49155 -U m2mpf m2m_pf ";

# 
my @sql_out_list;

sub getTableNameList{
    my ($sql_str) = @_;
    
    $sql_str =~ s/[\n\r]//g;
    my @sql_out = `echo "$sql_str" | $PSQL -t`;
    return @sql_out;
}

my $sql_str ="SELECT 
    wf_data_model.wf_data_model_name ,
    'wf_proc_data_' || to_hex(wf_data_model.wf_data_model_id) 
    FROM wf_data_model LEFT OUTER JOIN service_ap ON wf_data_model.service_ap_id = service_ap.service_ap_id
    WHERE service_ap_name = '$SERVICE_AP';";

push(@sql_out_list, getTableNameList($sql_str));

$sql_str = "SELECT 
    dev_data_model.dev_data_model_name ,
    'dev_raw_data_' || to_hex(dev_data_model.dev_data_model_id) 
    FROM dev_data_model
    LEFT OUTER JOIN dev_template ON dev_data_model.dev_template_id = dev_template.dev_template_id
    LEFT OUTER JOIN service_ap ON dev_template.service_ap_id = service_ap.service_ap_id
    WHERE service_ap.service_ap_name = '$SERVICE_AP'";

push(@sql_out_list, getTableNameList($sql_str));

my $out_path="$BIN_PATH/out/";
`mkdir -p $out_path`;
`chmod 777 $out_path`;
`mkdir -p $out_path/select`;
`chmod 777 $out_path/select`;
`mkdir -p $out_path/create`;
`chmod 777 $out_path/create`;

foreach my $line ( @sql_out_list ) {
    chomp $line;
    $line =~ s/  *\|  */,/g ;
    $line =~ s/^  *//g ;
    if ( $line eq "" ) { next ; }

    my ($table_id, $table_name) = split /,/, $line;
    print "$line       $table_id  =  $table_name\n";

    my $sql_str = "COPY ( select * from $table_name limit 1 ) TO '$out_path/$table_id.csv' CSV;";
    `echo "$sql_str" | $PSQL -t`;
}

foreach my $line ( @sql_out_list ) {
    chomp $line;
    $line =~ s/  *\|  */,/g ;
    $line =~ s/^  *//g ;
    if ( $line eq "" ) { next ; }
    my ($table_id, $table_name) = split /,/, $line;

    my $flg="false";
    foreach my $hoge (
             "gen2_ctrl_data",
             "gen2_bat_data",
             "gen2_pv_data",
             "gen2_load_data",
             "gen2_rect_data",
             "gen2_msr_15min",
             "gen2_msr_hour",
             "gen2_ctrl_get_pv_req",
             "gen2_ctrl_set_ctrl_res",
             "gen2_bat_get_pv_req",
             "gen2_pv_get_pv_req",
             "gen2_ctrl_event",
             "gen2_event_history") {
        if ( $hoge eq $table_id ) {
           $flg="true";
           last;
        }
    }
    if ( $flg eq "false" ) { next; }

    system "cp -p $out_path/$table_id.csv $out_path/select/";
    system "pg_dump -h 10.27.186.153 -p 49155 -U m2mpf m2m_pf -s -t $table_name > $out_path/create/$table_id.sql";
}

