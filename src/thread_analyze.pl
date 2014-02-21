#!/usr/bin/perl
#######################################################
#
# スレッドダンプの分析
#
#######################################################
package Main;
use strict;      # 変数宣言ミス等を厳格にチェック
use warnings;    # 警告を出力
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視

###################################
# スレッドダンプ１ファイルの処理
###################################
sub one_file_proc {
    my %kind_map = ();    # 分類毎のカウンタ
    my ( $file ) = @_;
    my $mode="false";     # false/DPX/DGW/WFC の３モード
    my $state;            # スレッド状態
    my $detail="other" ;  # 分類 (socketRead, jdbcとか)
    my $flag="false";     # 1ブロック解析中フラグ
    my $date = "";
    my $file_fh;
    my $all_cnt=0;        # スレッドの総数カウンタ
    open( $file_fh, "<", $file );
    while ( my $line = readline($file_fh) ) {
        chomp $line;
        if ( $date eq "" ) { $date = $line; next; } # 1行目の時刻保持

        if ( $line =~ /^"/ ) { $all_cnt += 1; };    # スレッド総数カウンタ++

        if ( $line =~ /^$/ && $mode ne "false" ) { # 空行の場合
#            printf ("$mode, $state, $detail\n");
            my $kind_key = "$mode-$state-$detail";
            my $kind_count = $kind_map{"$kind_key"};
            if (! $kind_count ) {
                $kind_count = 0;
            }
            $kind_count ++ ;
            $kind_map{"$kind_key"} = $kind_count;
            $detail = "other"; # 詳細を"other"で初期化
            next;
        }

        if ( $line =~ /^"http-/ ) { # DPX
             $mode="DPX";
             $flag="true";
             if ( $line =~ / runnable / ) {
                $state = "run"; 
             }
             if ( $line =~ / Object.wait/ ) {
                $state = "wait"; 
             }
             next;
        } elsif ( $line =~ /^"pool-22/ ) {   # DGW
             $mode="DGW";
             $flag="true";
             if ( $line =~ / runnable / ) {
                $state = "run"; 
             }
             if ( $line =~ / Object.wait/ ) {
                $state = "wait"; 
             }
             if ( $line =~ /waiting on condition/ ) {
                $state = "wait"; 
                $detail = "other";
                $flag = "false";
                next;
             }
             next;
        } elsif ( $line =~ /^"RMI TCP Connection/ ) {   # WFC
             $mode="WFC";
             $flag="true";
             if ( $line =~ / runnable / ) {
                $state = "run"; 
             }
             if ( $line =~ / Object.wait/ ) {
                $state = "wait"; 
             }
             if ( $line =~ /waiting on condition/ ) {
                $state = "wait"; 
                $detail = "other";
                $flag = "false";
                next;
             }
             next;
        } elsif ( $line =~ /^"/ ) {   # DPX/DGW/WFC以外
            $mode = "false";
            next;
        }

        # DPX/DGW/WFCのいずれでもない、もしくは、１ブロック解析済なら次の行へ
        if ( $mode eq "false" || $flag eq "false") { next; }
        
        if ( $line =~ /java.lang.Thread.State: BLOCKED/ ) {
            $state = "block";
            next;
        }

        # 分類抽出処理
        if ( $line =~ /socketRead/ ) {
            $detail = "socketRead";
            #$flag = "false"; # socketReadの場合、後でJDBCの場合もあるから解析フラグはfalseにしないでおく...
        } elsif ( $line =~ /socketAccept/ ) {
            $detail = "socketAccept";
            $flag = "false";
        } elsif ( $line =~ /before..*.convertBefore/ ) {
            $detail = "beforeConverter";
            $flag = "false";
        } elsif ( $line =~ /DbQueueReceiver.receiveMessage/ ) {
            $detail = "DbQueueReceiver";
            $flag = "false";
        } elsif ( $line =~ /[Jj]dbc/ ) {
            $detail = "JDBC";
            $flag = "false";
        } elsif ( $line =~ /org.apache.tomcat.util.net.JIoEndpoint/ ) {
            $detail = "tomcat";
            $flag = "false";
        }
    }
    eval{close($file);};

    $kind_map{"thread-all-count"} = $all_cnt;    # スレッド総数を分類ハッシュマップに追加

    my $result = "date=$date,file=$file,";
    foreach my $kind_key ( sort(keys %kind_map) ) {
        my $kind_cnt = $kind_map{"$kind_key"};
        $result = "$result$kind_key=$kind_cnt,";
    }
    return $result;
}

########################
# main
########################
# 引数で一つだけ指定された場合は１つだけ処理。分類は改行で表示
my ($ARG1) = @ARGV;
if ($ARG1) {
    foreach my $result (split /,/, one_file_proc($ARG1)) {
        print "$result\n";
    }
    exit;
}

# 全ファイル処理
my @result_list = ();                     # 全分析結果データの保持用データリスト
#my $debug_cnt=0;                       # debug用カウンタ
foreach my $file (`ls jstack*.log`) {
#    $debug_cnt ++ ;
#    if ( $debug_cnt == 20 ) { last; }
    chomp $file;
    my $result=one_file_proc($file);
    push ( @result_list, $result);
}

# ヘッダ生成(全データをなめて、分類を一覧にする）
my %key_map = ();
foreach my $result ( @result_list ) {
    my @tmp_list = split /,/, $result;
    foreach my $key ( @tmp_list ) {
        $key =~ s/=.*$// ;
        $key_map{"$key"} = "";
    }
}

# 固定ヘッダ追加
$key_map{"DPX-block-other"} = "";
$key_map{"DPX-run-other"} = "";
$key_map{"DPX-wait-other"} = "";
$key_map{"DGW-block-other"} = "";
$key_map{"DGW-run-other"} = "";
$key_map{"DGW-wait-other"} = "";
$key_map{"WFC-block-other"} = "";
$key_map{"WFC-run-other"} = "";
$key_map{"WFC-wait-other"} = "";

# 特殊ソートするために、一旦キー冒頭に番号を振る
my @head_tmp_list=();            # ヘッダの特殊ソート用一時リスト
for my $key (keys %key_map) {
    if      ( $key eq "date" ) {
        $key = "01-$key";
    } elsif ( $key eq "file" ) {
        $key = "02-$key";
    } elsif ( $key =~ "^DPX-" ) {
        $key = "03-$key";
    } elsif ( $key =~ "^DGW-" ) {
        $key = "04-$key";
    } elsif ( $key =~ "^WFC-" ) {
        $key = "05-$key";
    } elsif ( $key =~ "^thread-all-cnt" ) {
        $key = "99-$key";
    }
    push ( @head_tmp_list, $key ) ;
}

# 特殊ソート用につけた番号を除去しつつ、ヘッダリストを再構成する。
my @head_list = ();              # ヘッダのリスト
for my $key (sort(@head_tmp_list)) {
    $key =~ s/^[0-9][0-9]-//;
    push (@head_list, $key);
}

# ヘッダ出力
foreach my $key ( @head_list ) {
    print "$key,";
}
print "\n";

# データ出力
foreach my $result ( @result_list ) {
    my @data_list = split /,/, $result;
    foreach my $head_key ( @head_list ) {
        my $match_value="";
        foreach my $data ( @data_list ) {
            my ( $data_key, $data_value ) = split /=/, $data;
            if( $data_key eq $head_key ) {
                $match_value=$data_value;
                last;
            }
        }
        print "$match_value,";
    }
    print "\n";
}

