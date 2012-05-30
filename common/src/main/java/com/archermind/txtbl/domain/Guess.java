package com.archermind.txtbl.domain;

public class Guess {
	private int id = 0;

	private String name = "";

	private String commnet = "";

	private String guess_time = "";
	public String getCommnet() {
		return commnet;
	}
	public void setCommnet(String commnet) {
		this.commnet = commnet;
	}
	public String getGuess_time() {
		return guess_time;
	}
	public void setGuess_time(String guess_time) {
		this.guess_time = guess_time;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
