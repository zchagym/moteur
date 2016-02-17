package maain.models;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import maain.diskIO.DiskIO;

public class Dictionnaire 
{
	private final static String DEFAULT_URL = "https://fr.wiktionary.org/wiki/Wiktionnaire:10000-wp-fr",
								DEFAULT_PATTERN = "div#mw-content-text > ul > li > a[href]";
	private String wikiLinksUrl ,
				   wikiLinksPattern ;
	
    private static final String PREFIXE = "https://fr.wiktionary.org";
    
    private DiskIO out;
    private int maxLinks;
    
    public Dictionnaire(int _maxLinks) {
    	this(DEFAULT_URL, DEFAULT_PATTERN, _maxLinks);
    }
    
    public Dictionnaire(String url, String pattern, int _maxLinks) {
    	maxLinks = _maxLinks;
    	wikiLinksUrl = url;
    	wikiLinksPattern = pattern;
    	out = new DiskIO();
    	try {
			sortDataFinal = getWordsFromLinks();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
   
    
    private Elements getDicoLinksFromWiki() throws java.io.IOException 
    {
        Document doc = Jsoup.connect(wikiLinksUrl).get();
        Elements ul = doc.select(wikiLinksPattern);
        return ul;
    }

    private List<String> addPrefixToWikiLinks() throws java.io.IOException 
    {
    	List<String> lin = new ArrayList<String>();
    	Elements links = getDicoLinksFromWiki();
    	for (Element el : links)
    		lin.add(PREFIXE+el.attr("href"));	
    	return lin;
    }
    
    public List<String> getWordsFromLinks() throws java.io.IOException 
    {
    	List<String> words = new ArrayList<String>(),
    			     links = new ArrayList<String>();
    	links = addPrefixToWikiLinks();
    	links = links.subList(0, maxLinks);
    	for (String link : links) {
    		words.addAll(getWordsFromLink(link));
    	}
    	Collections.sort(words);
    	List<String> stopWords = out.getDataFromFile("mots_vides");
    	//int i1 = words.size();
    	words.removeAll(stopWords);
    	//int i2 = words.size();
    	//System.out.println("New Size "+i1);
    	out.writeToFile(filterWordsByLength(words, 2), "stopWords");
    	//System.out.println("Size[stop]: "+stopWords.size()+"\nSize[words]: "+i1+" --> "+i2+" dif: "+(i1-i2));
    	out.writeToFile(words, "words");
    	
    	return words;
    }
    
    private List<String> getWordsFromLink(String link) throws java.io.IOException
    {
    	List<String> words = new ArrayList<String>();
        Elements ul = Jsoup.connect(link).get().select("div#mw-content-text > ul > li > a[href]");
        for (Element el: ul) {
        	String str = wordWithoutApos(el.text());
        	words.add(StringUtils.trim(Normalizer
                    .normalize(str.toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    //.replaceAll("\\w'", "")
                    )
        	);
        }
        //Collections.sort(words);
        
        return words;
    }
    
    public String getWordRoot(String word) throws java.io.IOException
    {
    	return Jsoup
    	.connect("http://www.cnrtl.fr/definition/"+word).get()
    	.select("div#vtoolbar > ul > li#vitemselected > a > span")
    	.first().text().replaceAll("[\\d]","").toLowerCase();
    }
    
    public List<String> filterWordsByLength(List<String> words, int maxLength)
    {
    	System.out.println("filter...");
    	List<String> result = new ArrayList<String>();
    	Iterator<String> stringIterator = words.iterator();
    	while (stringIterator.hasNext()) {
    	    String string = stringIterator.next();
    	    if (string.length() <= maxLength || string.matches("^(\\W|-+)\\w*")) {
    	        //System.out.println(string);
    	        stringIterator.remove();
    	        result.add(string);
    	    }
    	}
    	
    	return result;
    }
    
    public String wordWithoutApos(String word)
    {
    	String sep ="'", res[] = word.split(sep);
    	if (res.length<2)
    	{
    		sep = " ";
    		res = word.split(sep);
    	}
    	if (res.length>=2 && res[0].length()<=2)
    	{
    		//System.out.println("Apos["+sep+"]: l="+res.length+" "+res[0]+" - "+res[1]);
    		return res[1];
    	}
    	else return word;
    }
    
    /**
     * TO DO 
     * @param words
     */
    public void removeStopWords(List<String> words)
    {
    	
    }
    
    /* --------------------------------------- */
    private List<String> sortDataFinal = new ArrayList<String>();
    public List<String> getSortDataFinal() {
		return sortDataFinal;
	}

	public void setSortDataFinal(List<String> sortDataFinal) {
		this.sortDataFinal = sortDataFinal;
	}

}