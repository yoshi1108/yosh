#!/usr/bin/perl
use strict;                 # 変数宣言ミス等を厳格にチェック
use warnings;               # 警告を出力
use Time::Local;

my ($FILE) = @ARGV;

my $start_time=`sort $FILE | head -1`;
chomp $start_time;
$start_time =~ s/\..*$// ;
my $start_unix_time = getUnixtime($start_time);
my $oldGrepStr="";
for (my $i=0; $i< 120 ; $i ++ ) {
    my ($sec,$min,$hour,$mday,$mon,$year) = (localtime($start_unix_time));
    my $grepStr = sprintf( "%04d/%02d/%02d %02d:%02d:%02d", $year + 1900, $mon + 1, $mday, $hour, $min , $sec);
    $grepStr =~ s/.$//g ;
    if ( $oldGrepStr ne $grepStr ) {
        my $cnt=`grep -c '$grepStr' $FILE`;
        chomp $cnt;
        print "$grepStr, $cnt\n";
        $oldGrepStr = $grepStr;
    }
    $start_unix_time += 10;
}

# yyyy/mm/dd HH:MM:SSをunixtimeに変換
sub getUnixtime{
    my ($target) = @_;
    my ($date, $time) = split / /, $target;
    my ($yyyy, $mm, $dd) = split /\//, $date;
    my ($HH, $MM, $SS) = split /:/, $time;
    return timelocal( $SS, $MM, $HH, $dd, $mm - 1, $yyyy - 1900 );
}