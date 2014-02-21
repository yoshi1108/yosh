#!/usr/bin/perl

########################
#
# 設定値データ作成スクリプト
#
########################
use strict;
use warnings;

my $max_count = 10000;

# コントロール設定値作成
open(CTRL_FH,">", "gen2_ctrl_config_insert_10000.sql");
for (my $var_ctrl = 1; $var_ctrl <= $max_count; $var_ctrl++) {
	my $ctrl_id = sprintf("999-%010d" ,$var_ctrl);
	print CTRL_FH "
INSERT INTO wf_proc_data_52dc906000000004  (  ctrl_id , ope_mod_com , om_po_t , cur_val_com , cv_po_t , acc_val1_com , load_collect_interval , pcs_collect_interval , lib_collect_interval , grid_collect_interval , pv_collect_interval , contracted_ampacity , sys_param1_ovr_vol , sys_param1_ovr_interval , sys_param2_uvr_vol , sys_param2_uvr_interval , sys_param3_ofr_frq_50hz , sys_param3_ofr_frq_60hz , sys_param4_ufr_frq_50hz , sys_param4_ufr_frq_60hz , sys_param5_ofr_interval , sys_param5_ufr_interval , sys_param6_ovr_ctrl_vol , sys_param6_phjmp_deg , sys_param7_delay_recov , enable_log_send , log_send_type , log_send_time , enable_sch_errorlog , enable_sch_measurelog , enable_sch_stalog , enable_sch_acclog , enable_sch_cyclog , log_level , frequency , wait_time_self_chk , back_light_off_time , retry_interval_reset , retry_count_reset , datetime  ) VALUES (  \'$ctrl_id\' , 1 , 15 , 1 , 999 , 1 , 8888 , 5555 , 5555 , 1111 , 1111 , 11 , 111 , 0.1 , 111 , 1.1 , 11.1 , 11.1 , 11.1 , 11.1 , 1.1 , 1.1 , 111 , 11 , 111 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , 1 , '2012-02-02 10:00:00+0900'  );";
}
close(CTRL_FH);

# 蓄電池設定値作成
open(BAT_FH ,">", "gen2_bat_config_insert_10000.sql");
for (my $var_bat = 1; $var_bat <= $max_count; $var_bat++) {
	my $dev_id = sprintf("999-%010d-001-0000001" ,$var_bat);
	print BAT_FH "
INSERT INTO wf_proc_data_52dc906000000008(   dev_id  , chg_st_time  , dischg_st_time  , enable_init_test  , bat_ope_mode  , pw_sply_priori_mode  , peakcut_pw  , const_cur  , const_vol  , const_chg_pw_u  , const_chg_pw_v  , const_dischg_pw_u  , const_dischg_pw_v  , dischg_end_vol  , low_bat_rate_normal  , low_bat_rate_emergency  , guard_lev_over_vol  , guard_lev_low_vol  , bat_correct_interval  , prechg_vol  , bat_ope_cmd  , bat_chg_cmd  , bat_chg_cap  , bat_chg_rate  , bat_chg_tim  , bat_dischg_cmd  , bat_dischg_cap  , bat_dischg_rate  , bat_dischg_tim  , datetime  , dev_fw_ver  , bat_cap  , dev_fw_ver_2nd  , dev_fw_ver_3rd  , dev_fw_ver_4th  , begin_date) VALUES (   $dev_id  , 11  , 55  , 1  , 1  , 10  , 23000  , 12.5  , 150  , 2200  , 1000  , 900  , 1200  , 55  , 0  , 20  , 120  , 100  , 256  , 200  , 1  , 2  , 12  , 123  , 22  , 1  , 23  , 123  , 22  , '2012-02-02 10:00:00+0900'  , 'v1.8'  , 22  , 'v1.0'  , 'v1.0'  , 'v1.0'  , '2012-02-02 10:00:00+0900'); ";
}
close(BAT_FH);

# PV設定値作成
open(PV_FH ,">", "gen2_pv_config_insert_10000.sql");
for (my $var_pv = 1; $var_pv <= $max_count; $var_pv++) {
	my $dev_pv_id = sprintf("999-%010d-001-0000001" ,$var_pv);
	print PV_FH "
INSERT INTO wf_proc_data_52dc90600000000a(   dev_id  , datetime  , enable_pv_link  , pv_link_mode  , pv_peakcut_pw  , pv_self_out_pw) VALUES (   $dev_pv_id  , '2012-02-02 10:00:00+0900'  , 21  , 1  , 8800  , 3300); "; 
}
close();