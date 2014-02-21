#!/usr/bin/perl
package DataConf;

our @table_id_list=();

#push(@table_id_list, ['raw' ,'コントローラ受信'                      ,'gen2_ctrl_data'        , '25','3000']);
#push(@table_id_list, ['raw' ,'蓄電池受信'                            ,'gen2_bat_data'         , '25','4000']);
#push(@table_id_list, ['raw' ,'PV受信'                                ,'gen2_pv_data'          , '25','4000']);
#push(@table_id_list, ['raw' ,'負荷受信'                              ,'gen2_load_data'        , '25','4000']);
#push(@table_id_list, ['raw' ,'整流器受信'                            ,'gen2_rect_data'        , '25','4000']);
#push(@table_id_list, ['proc','実績値15分'                            ,'gen2_msr_15min'        ,'169','6760']);
#push(@table_id_list, ['proc','実績値時間'                            ,'gen2_msr_hour'         ,'169','1690']);
#push(@table_id_list, ['raw' ,'コントローラ要求情報格納'              ,'gen2_ctrl_get_pv_req'  , '48',   '2']);
#push(@table_id_list, ['raw' ,'コントローラデバイス制御リクエスト結果','gen2_ctrl_set_ctrl_res', '48',   '2']);
#push(@table_id_list, ['raw' ,'蓄電池要求情報格納'                    ,'gen2_bat_get_pv_req'   , '48',   '2']);
#push(@table_id_list, ['raw' ,'PV要求情報格納'                        ,'gen2_pv_get_pv_req'    , '48',   '2']);
#push(@table_id_list, ['raw' ,'コントローラ受信イベント'              ,'gen2_ctrl_event'       , '48',  '50']);
#push(@table_id_list, ['proc','イベント情報'                          ,'gen2_event_history'    , '48',  '50']);



#                     テーブル名       ,                        テーブルID             , 時間, レコード数 ,シリアルIDパターン
push(@table_id_list, ['raw' ,'コントローラ受信'                      ,'gen2_ctrl_data'        , '25','300000', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'蓄電池受信'                            ,'gen2_bat_data'         , '25','400000', '999-00000$CNT$-001-0000001']);
push(@table_id_list, ['raw' ,'PV受信'                                ,'gen2_pv_data'          , '25','400000', '999-00000$CNT$-002-0000001']);
push(@table_id_list, ['raw' ,'負荷受信'                              ,'gen2_load_data'        , '25','400000', '999-00000$CNT$-003-0000001']);
push(@table_id_list, ['raw' ,'整流器受信'                            ,'gen2_rect_data'        , '25','400000', '999-00000$CNT$-004-0000001']);
push(@table_id_list, ['proc','実績値15分'                            ,'gen2_msr_15min'        ,'169','676000', '999-00000$CNT$']);
push(@table_id_list, ['proc','実績値時間'                            ,'gen2_msr_hour'         ,'169','169000', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'コントローラ要求情報格納'              ,'gen2_ctrl_get_pv_req'  , '48',   '280', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'コントローラデバイス制御リクエスト結果','gen2_ctrl_set_ctrl_res', '48',   '280', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'蓄電池要求情報格納'                    ,'gen2_bat_get_pv_req'   , '48',   '280', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'PV要求情報格納'                        ,'gen2_pv_get_pv_req'    , '48',   '280', '999-00000$CNT$']);
push(@table_id_list, ['raw' ,'コントローラ受信イベント'              ,'gen2_ctrl_event'       , '48',  '5000', '999-00000$CNT$']);
push(@table_id_list, ['proc','イベント情報'                          ,'gen2_event_history'    , '48',  '5000', '999-00000$CNT$']);



