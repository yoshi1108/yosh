#!/usr/bin/perl
#######################################################
#
# AccessLogの[Start] の時間を抽出する
#
# ファイル出力にした場合、結果はresultに格納される
# 
#######################################################

use strict;                 # 変数宣言ミス等を厳格にチェック 
use warnings;               # 警告を出力
use Time::Local;

### ACCESSログファイルディレクトリ
my $targetDir = "/opt/jboss/jboss/jboss-as/server/default/logs";

### ACCESSログファイル名
my $targetName = "M2MPF_ACCESS.log";

### outputファイル出力場所
my $outPutDir  = "/var/m2m_install/an/tool/accessLogFilter/result";
###  outputファイル名 [access_yyyymmddss(プログラム実行時間).csv]
my ($sec,$min,$hour,$mday,$mon,$year) = (localtime(time))[0..5];
my $outPutFile = sprintf( "access_%04d%02d%02d_%02d%02d%02d.csv", $year + 1900, $mon + 1, $mday, $hour, $min , $sec);
$outPutFile =  $outPutDir . "/" .$outPutFile;

### 引数チェック
if (@ARGV != 3){
     print "  引数に「開始時間」「終了時間」「標準出力 0/ファイル出力 1」を指定\n";   
     print "例）#perl accessLogFilter.pl '2012/12/03 10:00:00' '2025/12/31 12:00:00' 0 \n";
     exit;
}

#my $START_TIME = `date -d '$ARGV[0]' +'%s'`; # これだとかなり遅かったので変更
my $START_TIME = getUnixTime($ARGV[0]); 
my $END_TIME   = getUnixTime($ARGV[1]);
my $isOutPut = $ARGV[2];                # ファイル出力なし/あり

### 分析対象を出力
my $logListStr = `ls -t $targetDir/$targetName*`;
print "■分析対象リスト  \n";
print $logListStr;
my @LogList = split(/\n/, $logListStr); # \n改行で分解

### outputファイル出力
if ($isOutPut) {
    open( OUT, "> $outPutFile");
}

foreach my $fileName (@LogList) {
    open( IN, "< $fileName" );
    my @alldata = <IN>;

    ### ファイルを1行ずつなめる
    foreach my $data (@alldata) {
        # [Start]を含む列だけを抜き出す
        if ($data =~ /\[Start\]/) {
            # INFOより後方を削除し、時刻部分を取り出す
            $data =~ s/INFO.*//g;
            chomp $data;
            # dataの中のミリ秒を取り出す
            my $secData = $data;
            $secData =~ s/\..*//; 
            
            my $timeData = getUnixTime($secData); 
            #  時刻が期間内かを判定
            if ($timeData >= $START_TIME && $timeData <= $END_TIME) {
                if ($isOutPut) {
                    print OUT $data;
                    print OUT "\n";
                } else {
                    print "$data";
                    print "\n";
                }
            }
        } 
    }
close(IN);
}

if ($isOutPut) {
    close(OUT);
}

##########################################
# yyyy/MM/dd hh:mm:ss をUnixタイムに変換
##########################################
sub getUnixTime {
    my ( $utime ) = @_;    
    my($year, $month, $day, $hour, $minute, $second);
    $utime =~ m{^\s*(\d{1,4})\W*0*(\d{1,2})\W*0*(\d{1,2})\W*0*
                 (\d{0,2})\W*0*(\d{0,2})\W*0*(\d{0,2})}x; 
    $year = $1;  $month = $2;   $day = $3;
    $hour = $4;  $minute = $5;  $second = $6;
    $year = ($year<100 ? ($year<70 ? 2000+$year : 1900+$year) : $year);
    return timelocal($second,$minute,$hour,$day,$month-1,$year) . "\n";
}
