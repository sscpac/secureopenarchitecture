package mil.js.soaf.database;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public interface ResultPage<T> {

	@XmlElement
	public List<T> getResults();

	public void setResults(List<T> results);

	@XmlElement
	public long getCount();

	public void setCount(long count);

	public String toString();

}