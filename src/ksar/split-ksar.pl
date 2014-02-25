#!/usr/bin/perl
use strict;      # �ϐ��錾�~�X�������i�Ƀ`�F�b�N
use warnings;    # �x�����o��
no warnings qw(once);    # ��񂵂����p���ĂȂ��ϐ��ւ̌x��(�ݒ�l�ŕ��ʂɏo�Ă��܂�)�𖳎�
        
my $KSAR_JAR="/usr/local/kSar-5.0.6/kSar.jar";

#use File::Spec;
#use Math::BigFloat;
use Time::Local;
#use bigint;      # int��64bit�Ōv�Z
#use threads;
#use Time::HiRes qw( usleep gettimeofday tv_interval );

sub get_split_cnt {
    my ($file_fh) = @_;
    seek ($file_fh, 0, 0); # �t�@�C���ǂݍ��݈ʒu��`���ɖ߂�
    # sar�t�@�C���̓��܂����萔�̎��O�`�F�b�N
    my $split_cnt=1;            # �����t�@�C���̃J�E���^���Z�b�g
    my $old_time="999999";      # ���܂�������O�񎞍�
    while ( my $line = readline($file_fh) ) {
        chomp $line;

        #print "$line\n";

        if ( $line =~ /^Average.*/ ) {
            last;    #
        }

        # �����f�[�^����Ȃ��ꍇ

        if ( $line eq "" ) { next; }
        my ($time) = split / /, $line;
        $time =~ s/://g;

        # �����f�[�^����Ȃ��ꍇ
        if ( $time =~ /[^0-9]/ ) { next; }

        if ( $old_time eq "999999" ) {
            $old_time = $time;
        }

        # ���܂�����������:
        if ( $time < $old_time ) {
            $split_cnt++;
        }
        $old_time = $time;
    }
    seek ($file_fh, 0, 0); # �t�@�C���ǂݍ��݈ʒu��`���ɖ߂�
    print "split_cnt=$split_cnt\n";
    return $split_cnt;
}

sub oneFile {
    my ( $start, $end, $file ) = @_; # �J�n���� �I������ �ǂݍ���sar�t�@�C����
    my $file_fh;
    open( $file_fh, "<", "$file" );

    # sar�t�@�C���̓��܂����萔�̎��O�`�F�b�N
    my $split_cnt = get_split_cnt($file_fh); # �����t�@�C����

    my $line_cnt=0;             # ���C���J�E���^���Z�b�g
    my $file_cnt=1;             # �����t�@�C���̃J�E���^
    my $head_flg="false";
    my $head_str="";            # �e�v�f�̃w�b�_
    my $old_time="999999";      # ���܂�������O�񎞍�
    my %file_map;               # ���t���t�@�C���̃n�b�V���}�b�v
    
    my $all_head="";
    # sar�t�@�C����͏���
    my $time;

    # �t�@�C�����̎��ۂ̊J�n�A�I������
    my $file_start_time="999999";
    my $file_end_time  ="999999";

    while ( my $line = readline($file_fh) ) {
        chomp $line;
        $line_cnt ++;
        if ( $line_cnt == 1 ) {
            $all_head = $line;
        }
        # �f�[�^��ʋ�؂�`�F�b�N
        if ( $line eq "" ) {
            $file_cnt = 1;      # ��s������J�E���^�����Z�b�g
            $head_flg = "true";
            $file_end_time=$old_time;
            $old_time="999999"; # ���܂�������O�񎞍������Z�b�g
            next;
        }

        # �����f�[�^�擾
        ($time) = split / /, $line; $time =~ s/://g;

        # ���������t�@�C���̃t�@�C���n���h���擾�i�Ȃ���ΐV�K�쐬) 
        my $tmp_fh = $file_map{$file_cnt};
        if (!$tmp_fh) {
            my $tmp_file = $file . "_" . $file_cnt;
            open( $tmp_fh, ">", "$tmp_file" );
            $file_map{$file_cnt} = $tmp_fh;
        }
   
        # �w�b�_���o�͏��� 
        if ( $head_flg eq "true" ) {
            $head_str = $line; # �w�b�_��ێ�
            $head_flg = "false";
            my ($head_time) = split / /, $head_str; $head_time =~ s/://g;
            # �w�b�_���o��
#            if ( $head_time <= $end ) {
                my $tmp_start = $start;
                $tmp_start =~ s/(..)(..)(..)/$1:$2:$3/;
                # �J�n�����w��̏ꍇ�A�w�b�_�̎������C������
                if ( $tmp_start ne "00:00:00" ) {
                    $head_str =~ s/^..:..:../$tmp_start/;
                }
                print $tmp_fh "\n$head_str\n"; # �w�b�_�ǉ�
#            }
            next;
        }
        # �����f�[�^����Ȃ��ꍇ�̏����iAverage�s�Ƃ��j
        if ( $time =~ /[^0-9]/ ) {
            print $tmp_fh "$line\n"; # �t�@�C���Ƀf�[�^�ǉ�
            next; # ���̂܂܂��Ⴞ�߁B�Ō�̃t�@�C���ɂ����ǉ�����Ȃ�
        }

        # �I�������`�F�b�N
        if ( $file_cnt == $split_cnt && $time > $end ) {
            $old_time="999999"; # ���܂�������O�񎞍������Z�b�g
            next;
        }
    
        if ( $old_time eq "999999" ) {
            $old_time = $time;
            $file_start_time=$time;
        }
    
        # ���܂�����������:
        if ( $time < $old_time ) {
            $file_cnt ++ ;
            $tmp_fh = $file_map{$file_cnt}; # �t�@�C���؂�ւ�
            if (!$tmp_fh) {
                my $tmp_file = $file . "_" . $file_cnt;
                open( $tmp_fh, ">", "$tmp_file" );
                $file_map{$file_cnt} = $tmp_fh;
    
                # Linux 2.6.18-194.el5 (ncpla6t005)       01/31/14
                my $tmp_date = $all_head;
                $tmp_date =~ s/^.* //;
                my ($o_mon, $o_mday, $o_year) = split /\//, $tmp_date;
                #                          �b  ��  ��   ��   ��    , �N
                my $unix_time = timelocal(   0, 0, 0, $o_mday, $o_mon - 1, $o_year - 1900 + 2000 );
                # �P������
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
    
                print $tmp_fh "$tmp_all_head\n"; # �I�[���w�b�_�ǉ�
            }
            my $tmp_head_str = $head_str;
            $tmp_head_str =~ s/..:..:.. /00:00:00 /g;
            print $tmp_fh "\n$tmp_head_str\n"; # �t�@�C���Ƀf�[�^�ǉ�
        }
        $old_time = $time;
        
        # �J�n�����`�F�b�N
        if ( $file_cnt == 1 && $time < $start) {
            next; # �����P�t�@�C���ڂŁA���A�J�n�������ȑO�̃f�[�^�̏ꍇ�͒ǋL���Ȃ�
        }
        
        print $tmp_fh "$line\n"; # �t�@�C���Ƀf�[�^�ǉ�
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

$START =~ s/://g;    # : ���폜���� 10:30:25 -> 103025
$END =~ s/://g;      # : ���폜����

# �P�t�@�C���w�莞
if ($FILE) {
    oneFile( $START, $END, $FILE );
    exit;
}

# �t�@�C���w�薳���̏ꍇ�A�J�����g�f�B���N�g����ksar-*.log�̃t�@�C�����X�g��Ώۂɂ���
foreach my $file ( glob("ksar-*.log") ) {
    chomp $file;
    oneFile( $START, $END, $file );
}

