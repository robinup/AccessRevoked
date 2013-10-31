#! /usr/bin/perl -w

use Time::HiRes qw(gettimeofday);

my @checkRslt = &checkResponse();
if ($checkRslt[0] < 0) {
  # No response or bad response
  print "CRITICAL - OptSOA check failure. \nCommand: $checkRslt[2]\nResult: $checkRslt[1]\n";  
  exit(2);
} elsif ($checkRslt[0] < 100) {
  # Everything good
  print "OK - OptSOA check gooddd\n";
  exit(0);
} 

# We might hit the GC freeze. Let's try again
sleep(1);

@checkRslt = &checkResponse();
if ($checkRslt[0] < 0) {
  # No response or bad response
  print "CRITICAL - OptSOA check failure. \nCommand: $checkRslt[2]\nResult: $checkRslt[1]\n";
  exit(2);
} elsif ($checkRslt[0] < 100) {
  # Everything good
  print "OK - OptSOA check good\n";
  exit(0);
} else {
  print "CRITICAL - OptSOA check taking too long -- " . $checkRslt[0] . "milliseconds to finish\n";
  exit(2);
} 



## Return --- 
##  -1 empty result
##  -2 error result
##  -3 other errors
##  positive number - the time used
sub checkResponse(){
  my $cmd = "curl \"http://localhost:8888/?command=offerwall&algorithm=324&source=offerwall&platform=iOS&device_type=iphone&udid=12345&offer_count=10&ip_addr=38.104.224.62\"";
  my $starttime = gettimeofday;
  my $result = `$cmd`;

  if (length($result) == 0) {
    return (-1, "", $cmd);
  } elsif ($result =~ /ErrCode\s\d/){
    return (-2, $result, $cmd);
  } elsif ($result !~ /\{\"id\"\:\"[\w\-]+\"\,\"rank_index\":\d+\,\"auditioning\":\d\,\"rank_score\"\:[\d\.]+\}/){
    return (-3, $result, $cmd);
  }

  #print "The query result is \n" . $result . "\n";
  my $endtime = gettimeofday;
  my $timecost = ($endtime - $starttime) * 1000; 
  return ($timecost); 
}

