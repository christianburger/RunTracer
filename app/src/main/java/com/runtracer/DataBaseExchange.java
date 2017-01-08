package com.runtracer;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

class DataBaseExchange implements Serializable, Cloneable {
	private static final long serialVersionUID = 100L;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	URL url;
	String command = "";
	String accountEmail = "";
	String full_name = "";
	String hash= "";

	JSONObject json_data_in = new JSONObject();
	JSONObject json_data_out = new JSONObject();

	int error_no=0;
	boolean pending= false;

	/**
	 * Creates and returns a copy of this {@code Object}. The default
	 * implementation returns a so-called "shallow" copy: It creates a new
	 * instance of the same class and then copies the field values (including
	 * object references) from this instance to the new instance. A "deep" copy,
	 * in contrast, would also recursively clone nested objects. A subclass that
	 * needs to implement this kind of cloning should call {@code super.clone()}
	 * to create the new instance and then create deep copies of the nested,
	 * mutable objects.
	 *
	 * @return a copy of this object.
	 * @throws CloneNotSupportedException if this object's class does not implement the {@code
	 *                                    Cloneable} interface.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	String getHash() {
		String hash= null;
		try {
			hash = this.clone().toString();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		assert hash != null;
		this.hash= String.valueOf((hash.hashCode()));

		return (this.hash);
	}

	private DataBaseExchange() {
	}

	public DataBaseExchange(URL url, String command, String accountEmail, String full_name, String hash, JSONObject json_data_in, JSONObject json_data_out, int error_no, boolean pending) {
		this.url = url;
		this.command = command;
		this.accountEmail = accountEmail;
		this.full_name = full_name;
		this.hash = hash;
		this.json_data_in = json_data_in;
		this.json_data_out = json_data_out;
		this.error_no = error_no;
		this.pending = pending;
	}

	private URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	private String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	private String getAccountEmail() {
		return accountEmail;
	}

	public void setAccountEmail(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	private JSONObject getJson_data_in() {
		return json_data_in;
	}

	public void setJson_data_in(JSONObject json_data_in) {
		this.json_data_in = json_data_in;
	}

	private JSONObject getJson_data_out() {
		return json_data_out;
	}

	public void setJson_data_out(JSONObject json_data_out) {
		this.json_data_out = json_data_out;
	}

	private int getError_no() {
		return error_no;
	}

	public void setError_no(int error_no) {
		this.error_no = error_no;
	}

	private boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	static DataBaseExchange createDataBaseExchange() {
		return new DataBaseExchange();
	}

	@Override
	public String toString() {
		return "DataBaseExchange{" +
			"url=" + url +
			", command='" + command + '\'' +
			", accountEmail='" + accountEmail + '\'' +
			", full_name='" + full_name + '\'' +
			", hash='" + hash + '\'' +
			", json_data_in=" + json_data_in +
			", json_data_out=" + json_data_out +
			", error_no=" + error_no +
			", pending=" + pending +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DataBaseExchange)) return false;
		DataBaseExchange that = (DataBaseExchange) o;
		return getError_no() == that.getError_no() && isPending() == that.isPending() && getUrl().equals(that.getUrl()) && getCommand().equals(that.getCommand()) && getAccountEmail().equals(that.getAccountEmail()) && getFull_name().equals(that.getFull_name()) && getHash().equals(that.getHash()) && getJson_data_in().equals(that.getJson_data_in()) && getJson_data_out().equals(that.getJson_data_out());
	}

	@Override
	public int hashCode() {
		int result = getUrl().hashCode();
		result = 31 * result + getCommand().hashCode();
		result = 31 * result + getAccountEmail().hashCode();
		result = 31 * result + getFull_name().hashCode();
		result = 31 * result + getHash().hashCode();
		result = 31 * result + getJson_data_in().hashCode();
		result = 31 * result + getJson_data_out().hashCode();
		result = 31 * result + getError_no();
		result = 31 * result + (isPending() ? 1 : 0);
		return result;
	}

	void clear() {
		try {
			url= new URL("https://www.runtrace.com");
			command = "empty";
			accountEmail = "empty";
			full_name = "empty";
			json_data_in = new JSONObject("{\"key\":\"data\"}");
			json_data_out= new JSONObject("{\"key\":\"data\"}");
			error_no=0;
			pending= false;
		} catch (JSONException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(this.url);
		out.writeObject(this.command);
		out.writeObject(this.accountEmail);
		out.writeObject(this.full_name);
		out.writeObject(this.hash);

		out.writeObject(this.json_data_in.toString());
		out.writeObject(this.json_data_out.toString());
		out.writeObject(this.error_no);
		out.writeObject(this.pending);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, JSONException {
		this.url= (URL) in.readObject();
		this.command= (String) in.readObject();
		this.accountEmail= (String) in.readObject();
		this.full_name= (String) in.readObject();
		this.hash= (String) in.readObject();

		this.json_data_in= new JSONObject((String) in.readObject());
		this.json_data_out= new JSONObject((String) in.readObject());
		this.error_no= (int) in.readObject();
		this.pending= (boolean) in.readObject();
	}
}
