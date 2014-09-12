#!/usr/bin/perl
use strict;                 # 変数宣言ミス等を厳格にチェック
use warnings;               # 警告を出力
use Time::Local;
use Math::BigInt;
use Math::BigFloat;
use Time::HiRes qw( usleep gettimeofday tv_interval );

use File::Spec;

print "### スクリプトの絶対パス、スクリプト名の取得\n";
my $BIN_PATH = File::Spec->rel2abs($0);
my $BIN_NAME = $BIN_PATH;
$BIN_NAME =~ s/^.*[\/\\]//g;
$BIN_PATH =~ s/[\/\\][^\/\\]*$//g;
Internals::SvREADONLY( $BIN_PATH, 1 );
Internals::SvREADONLY( $BIN_NAME, 1 );

print "BIN_PATH=" . ${BIN_PATH} . "\n";
print "BIN_NAME=" . ${BIN_NAME} . "\n";

print "### ファイルを1行ずつ処理\n";
my $file="${BIN_PATH}/${BIN_NAME}";
open( IN, "< $file" );
my @alldata = <IN>;
my $cnt=0;
foreach my $data (@alldata) {
	chomp $data;
	$cnt ++;
	if (! ( $data =~ /data/ ) ){
		# "data"文字を含まない場合は次へ
		next;
	}
	printf "%d:%s\n", $cnt, $data;
}
#####################################################

#############################################
# 通常変数のままの処理。# 32bitマシンなら4294967295以上はe+015とかなっちゃう
#############################################
print "### 通常変数のままの処理。大きい値はe+015とかなっちゃう\n";
my $iCnt=1118446744073700;
print "iCnt=" . $iCnt . "\n";

#############################################
# use bigint;すれば解消するが、全部の変数がbigintになり、関係ない箇所のループとかも遅くなる。
# 単なるファイル処理時のカウンタとかでも超遅くなる。
#############################################
print "### use bigintで処理。変数の扱いは良くなるが全体が遅くなる\n";
my $t0;
# マイクロ秒取得
$t0 = [gettimeofday];

use bigint;

$iCnt=1118446744073700;
print "iCnt=" . $iCnt . "\n";

# 1000までしか処理しないような変数iTmpも暗黙でbigint扱いになり、
# インクリメント等の処理が異常に遅くなる
for (my $iTmp=0; $iTmp < 1000; $iTmp++){
}
my $elapsed = tv_interval($t0);
printf "遅い elapsed=%f\n", $elapsed;

#############################################
# 全体bigint指定はやっぱりやめてBigIntを個別に指定
#############################################
print "### use bigintやめて、個別にBigInt指定\n";
$t0 = [gettimeofday];
no bigint;

# 大きい値を扱いたい変数だけ、BigIntで個別に定義
$iCnt=Math::BigInt->new(1118446744073700);

# 期待どおりの変数の扱いになるし
print "iCnt=" . $iCnt . "\n";

# 遅く無い。この場合、"no bigint"しているので、iTmpはbigintではない。
for (my $iTmp=0; $iTmp < 1000; $iTmp++){
}
$elapsed = tv_interval($t0);
printf "速い elapsed=%f\n", $elapsed;

# BigFloat
print "### 小数点の表示\n";
my $tmpBigFloat = Math::BigFloat->new(1)->fdiv(3);
my $tmpF = 1 / 3;
print "tmpF       =" . $tmpF. "\n";
print "tmpBigFloat=" . $tmpBigFloat . "\n";

sub hogeFunc {
	print "hoge\n";
}

hogeFunc();
