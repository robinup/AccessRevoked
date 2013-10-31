#! perl -w

###  Check if there are dup offer list in a offer list file ###

use strict;
my $dirr = "./";
opendir (DIR, $dirr) or die $!;

while(my $file = readdir(DIR)){
    print "Process $file \n";
    &findDup($file);
}


sub findDup{
 my $file = shift;
 my %uids = ();
 open (FILLE, $file) || die "Failed to open $_\n"; 
   while(<FILLE>){
     chomp;
     if($_ =~ /\{"rank_index"\:"\d+"\, "offer_id"\:"([\w\-]+)\"/){
        if(defined $uids{$1}){
            print "ERROR: $1 appeared as dup\n";
        }
        $uids{$1} = 1;
     }
   }
   close(FILLE);
}


