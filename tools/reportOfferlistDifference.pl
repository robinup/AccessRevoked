#! perl -w

use strict;

# Compare the file generated in two batches to see how much differences are there
# Used when code changes are introduced

scalar(@ARGV) == 3 || die "Usage: perl $0 yyyy-mm-dd hhmm1  hhmm2 \n\n";

my %diffReport = ();

my $date = $ARGV[0];
my $dir1 = $ARGV[1];
my $dir2 = $ARGV[2];

$dir1 = "/ebs/data/opt-java/$date/opt/$dir1";
$dir2 = "/ebs/data/opt-java/$date/opt/$dir2";

opendir(D1, $dir1) || die "Failed to open $dir1\n";
my @files1 = grep { (!/^\./)} readdir(D1);
close(D1);
opendir(D2, $dir2) || die "Failed to open $dir1\n";
my @files2 = grep { (!/^\./)} readdir(D2);
close(D2);

print "\nComparing $dir1 and $dir2\n\n";

foreach my $file1 (@files1) {
  $diffReport{$file1} = "Only appears in $dir1\n";
}

foreach my $file2 (@files2) {
  if (defined $diffReport{$file2} ){
    $diffReport{$file2} = &findDiff($file2);
  } else {
    $diffReport{$file2} = "Only appears in $dir2\n";
  }
}

foreach my $key (sort(keys %diffReport)) {
  print "$key  ==>  " . $diffReport{$key};
}

sub findDiff() {
  my $file = shift;
  my $f1 = "$dir1/$file";
  my $f2 = "$dir2/$file";

  my %overallRslt = ();
  my %fileSigs1 = ();
  open(F1, $f1) || die "Failed to open $f1\n";
  while (<F1>) {
      chomp;
      if ($_ =~ /^.*"rank_index":"(\d+)".*"offer_id":"([\w\-]+)".*"rank_score":"([\d\.]+)".*/) {
         $fileSigs1{$2} = {"index" => $1, "score" => $3};
      }
  }
  close(F1);

  my $overlapCount = 0;
  my $diffCount = 0;
  my $indexDiffAbs = 0;
  my $indexDiff = 0;
  my $scoreDiff = 0;
  my $f2OnlyCnt = 0;

  open(F2, $f2) || die "Failed to open $f2\n";
  while (<F2>) {
      chomp;
      if ($_ =~ /^.*"rank_index":"(\d+)".*"offer_id":"([\w\-]+)".*"rank_score":"([\d\.]+)".*/) {
         if (defined ($fileSigs1{$2})) {
           $overlapCount += 1;
           if ($1 != $fileSigs1{$2}{"index"}) {
             $diffCount += 1;
             $indexDiffAbs += abs($1 - $fileSigs1{$2}{"index"}); 
             $indexDiff += (abs($1 - $fileSigs1{$2}{"index"}) / $1) * 100; 
             $scoreDiff += (abs($3 - $fileSigs1{$2}{"score"}) / $3) * 100;
           } 
         } else {
           $f2OnlyCnt += 1;
         }
      }
  }
  close(F2);
 
  if ($diffCount != 0) {
    $indexDiffAbs /= $diffCount;
    $indexDiff /= $diffCount;
    $scoreDiff /= $diffCount;
  }

  return "DIFF - Offers only appear Dir1 Version: " . (scalar(keys %fileSigs1) - $overlapCount) . "; Offers only appear in Dir2 version: $f2OnlyCnt;\n                       For $overlapCount overlaps, $diffCount are different, The average absolute index shift is $indexDiffAbs; The average relative index shift is $indexDiff; the average score diff (%) is $scoreDiff\n"; 
}
