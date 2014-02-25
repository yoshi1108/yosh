#!/usr/bin/perl
use strict;      # 変数宣言ミス等を厳格にチェック
use warnings;    # 警告を出力
no warnings qw(once);    # 一回しか利用してない変数への警告(設定値で普通に出てしまう)を無視
        
my $KSAR_JAR="/usr/local/kSar-5.0.6/kSar.jar";

#use File::Spec;
#use Math::BigFloat;
use Time::Local;
#use bigint;      # intを64bitで計算
#use threads;
#use Time::HiRes qw( usleep gettimeofday tv_interval );

sub get_split_cnt {
    my ($file_fh) = @_;
    seek ($file_fh, 0, 0); # ファイル読み込み位置を冒頭に戻す
    # sarファイルの日またがり数の事前チェック
    my $split_cnt=1;            # 分割ファイルのカウンタリセット
    my $old_time="999999";      # 日またぎ判定前回時刻
    while ( my $line = readline($file_fh) ) {
        chomp $line;

        #print "$line\n";

        if ( $line =~ /^Average.*/ ) {
            last;    #
        }

        # 時刻データじゃない場合

        if ( $line eq "" ) { next; }
        my ($time) = split / /, $line;
        $time =~ s/://g;

        # 時刻データじゃない場合
        if ( $time =~ /[^0-9]/ ) { next; }

        if ( $old_time eq "999999" ) {
            $old_time = $time;
        }

        # 日またぎ発生処理:
        if ( $time < $old_time ) {
            $split_cnt++;
        }
        $old_time = $time;
    }
    seek ($file_fh, 0, 0); # ファイル読み込み位置を冒頭に戻す
    print "split_cnt=$split_cnt\n";
    return $split_cnt;
}

sub oneFile {
    my ( $start, $end, $file ) = @_; # 開始時刻 終了時刻 読み込むsarファイル名
    my $file_fh;
    open( $file_fh, "<", "$file" );

    # sarファイルの日またがり数の事前チェック
    my $split_cnt = get_split_cnt($file_fh); # 分割ファイル数

    my $line_cnt=0;             # ラインカウンタリセット
    my $file_cnt=1;             # 分割ファイルのカウンタ
    my $head_flg="false";
    my $head_str="";            # 各要素のヘッダ
    my $old_time="999999";      # 日またぎ判定前回時刻
    my %file_map;               # 日付毎ファイルのハッシュマップ
    
    my $all_head="";
    # sarファイル解析処理
    my $time;

    # ファイル内の実際の開始、終了時刻
    my $file_start_time="999999";
    my $file_end_time  ="999999";

    while ( my $line = readline($file_fh) ) {
        chomp $line;
        $line_cnt ++;
        if ( $line_cnt == 1 ) {
            $all_head = $line;
        }
        # データ種別区切りチェック
        if ( $line eq "" ) {
            $file_cnt = 1;      # 空行来たらカウンタをリセット
            $head_flg = "true";
            $file_end_time=$old_time;
            $old_time="999999"; # 日またぎ判定前回時刻をリセット
            next;
        }

        # 時刻データ取得
        ($time) = split / /, $line; $time =~ s/://g;

        # 分割したファイルのファイルハンドラ取得（なければ新規作成) 
        my $tmp_fh = $file_map{$file_cnt};
        if (!$tmp_fh) {
            my $tmp_file = $file . "_" . $file_cnt;
            open( $tmp_fh, ">", "$tmp_file" );
            $file_map{$file_cnt} = $tmp_fh;
        }
   
        # ヘッダ情報出力処理 
        if ( $head_flg eq "true" ) {
            $head_str = $line; # ヘッダを保持
            $head_flg = "false";
            my ($head_time) = split / /, $head_str; $head_time =~ s/://g;
            # ヘッダ情報出力
#            if ( $head_time <= $end ) {
                my $tmp_start = $start;
                $tmp_start =~ s/(..)(..)(..)/$1:$2:$3/;
                # 開始時刻指定の場合、ヘッダの時刻を修正する
                if ( $tmp_start ne "00:00:00" ) {
                    $head_str =~ s/^..:..:../$tmp_start/;
                }
                print $tmp_fh "\n$head_str\n"; # ヘッダ追加
#            }
            next;
        }
        # 時刻データじゃない場合の処理（Average行とか）
        if ( $time =~ /[^0-9]/ ) {
            print $tmp_fh "$line\n"; # ファイルにデータ追加
            next; # このままじゃだめ。最後のファイルにしか追加されない
        }

        # 終了時刻チェック
        if ( $file_cnt == $split_cnt && $time > $end ) {
            $old_time="999999"; # 日またぎ判定前回時刻をリセット
            next;
        }
    
        if ( $old_time eq "999999" ) {
            $old_time = $time;
            $file_start_time=$time;
        }
    
        # 日またぎ発生処理:
        if ( $time < $old_time ) {
            $file_cnt ++ ;
            $tmp_fh = $file_map{$file_cnt}; # ファイル切り替え
            if (!$tmp_fh) {
                my $tmp_file = $file . "_" . $file_cnt;
                open( $tmp_fh, ">", "$tmp_file" );
                $file_map{$file_cnt} = $tmp_fh;
    
                # Linux 2.6.18-194.el5 (ncpla6t005)       01/31/14
                my $tmp_date = $all_head;
                $tmp_date =~ s/^.* //;
                my ($o_mon, $o_mday, $o_year) = split /\//, $tmp_date;
                #                          秒  分  時   日   月    , 年
                my $unix_time = timelocal(   0, 0, 0, $o_mday, $o_mon - 1, $o_year - 1900 + 2000 );
                # １日たす
                $unix_time += ($file_cnt - 1 ) * 24 * 60 * 60;
                my ($sec, $min, $hour, $mday, $mon, $year) =  localtime($unix_time);
                $year += 1900 - 2000;
                $mon += 1;
                my $new_date = sprintf("%02d/%02d/%02d", $mon, $mday, $year);
    
                my $tmp_all_head = $all_head;
                print "$new_date $mon $mday $year\n";
                #$tmp_all_head =~ s%../../../%$new_date% ;
                $tmp_all_head =~ s/ [^ ]*$//;
                $tmp_all_head = $tmp_all_head . " " . $new_date;
                print "$tmp_all_head\n";
    
                print $tmp_fh "$tmp_all_head\n"; # オールヘッダ追加
            }
            my $tmp_head_str = $head_str;
            $tmp_head_str =~ s/..:..:.. /00:00:00 /g;
            print $tmp_fh "\n$tmp_head_str\n"; # ファイルにデータ追加
        }
        $old_time = $time;
        
        # 開始時刻チェック
        if ( $file_cnt == 1 && $time < $start) {
            next; # 分割１ファイル目で、かつ、開始時刻より以前のデータの場合は追記しない
        }
        
        print $tmp_fh "$line\n"; # ファイルにデータ追加
    }
    $file_start_time =~ s/(..)(..)(..)/$1:$2:$3/;
    $file_end_time =~ s/(..)(..)(..)/$1:$2:$3/;
    print "start time=$file_start_time end time=$file_end_time\n";
    
    eval{ close(file_fh); };

    make_pdf( $file );
}

sub make_pdf {
    my ( $file ) = @_;
    for (my $i=1;;$i++){
        my $tmp_file = $file . "_" . $i;
        if ( !-f $tmp_file ) {
            last;
        }
        my $pdf_file = $tmp_file . ".pdf";
        $pdf_file = modServerName($pdf_file);
        unlink $pdf_file;
        my $cmd = "java -jar $KSAR_JAR -input $tmp_file -outputPDF $pdf_file";
        print "$cmd\n";
        system ($cmd);
	#system ("explorer.exe $pdf_file");
    }
}

sub modServerName {
    my($host) = @_;
    return $host;
}

my ($START, $END, $FILE)=@ARGV;

if ( !$START ) { $START = "00:00:00"; }
if ( !$END )   { $END   = "23:59:59"; }

$START =~ s/://g;    # : を削除する 10:30:25 -> 103025
$END =~ s/://g;      # : を削除する

# １ファイル指定時
if ($FILE) {
    oneFile( $START, $END, $FILE );
    exit;
}

# ファイル指定無しの場合、カレントディレクトリのksar-*.logのファイルリストを対象にする
foreach my $file ( glob("ksar-*.log") ) {
    chomp $file;
    oneFile( $START, $END, $file );
}

