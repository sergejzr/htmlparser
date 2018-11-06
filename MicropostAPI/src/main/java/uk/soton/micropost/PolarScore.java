package uk.soton.micropost;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PolarScore {
Hashtable<String, Integer> postags=new Hashtable<>();
Hashtable<String, Integer> negtags=new Hashtable<>();
int cnttweets=0;
private double score;
public void addPosTag(String hashtag) {
	addTag(postags,hashtag);
	
}
private void addTag(Hashtable<String, Integer> postags2, String hashtag) {
	Integer cnt = postags2.get(hashtag);
	if(cnt==null) cnt=0;
	postags2.put(hashtag, cnt+1);
	
}
public void addNegTag(String hashtag) {
	addTag(negtags,hashtag);
	
}
public void addtweet() {
	cnttweets++;
	
}
public void setScore(double i) {
	this.score=i;
	
}
public double getScore() {
	return score;
}
}
