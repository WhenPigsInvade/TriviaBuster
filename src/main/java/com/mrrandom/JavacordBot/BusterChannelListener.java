package com.mrrandom.JavacordBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

public class BusterChannelListener {
	private final HashMap<String, String> qna;
	private final TextChannel channel;
	private final Message startPoint;
	private final ChannelListener listener;
	public BusterChannelListener(final HashMap<String, String> qna_, TextChannel c, Message startPoint_) {
		qna = qna_;
		channel = c;
		startPoint = startPoint_;
		
		listener = new ChannelListener(50); //poll every 50ms
		//wait for start cmd
	}
	public void start() {
		(new Thread(listener)).start();
		
		//self destruct in 10 mins
		(new Thread(new SelfDestruct(1000l*60*10))).start();
	}
	public void processMessage(Message m) {
		String q = Main.getQuestion(m.getContent());
		System.out.println(q + " | "+qna.getOrDefault(q, "no answer found"));
		if(q != null) {
			if(Main.mode == 1) try {Thread.sleep(500+(long)(Math.random()*700));} catch(Exception e) {}
			if(Main.mode == 2) try {Thread.sleep(1000+(long)(Math.random()*1000));} catch(Exception e) {}
			channel.sendMessage(qna.get(q));
		}
	}
	public void close() {
		listener.active = false;
	}
	public boolean isActive() {
		return listener.active;
	}
	public boolean equals(Object o) {
		if(o instanceof BusterChannelListener) {
			return ((BusterChannelListener) o).channel.equals(channel);
		}
		return false;
	}
	private class ChannelListener implements Runnable{
		private final int delay;
		public boolean active = true;
		private List<Message> previousMessages;
		public ChannelListener(int delay_) {
			delay = delay_;
			previousMessages = new ArrayList<Message>();
		}
		@Override
		public void run() {
			while(active) {
				//get the difference between previous polling and current list
				Stream<Message> msgs = channel.getMessagesAfterAsStream(startPoint);
				List<Message> msgList = msgs.collect(Collectors.toList());
				msgList.removeAll(previousMessages);
				previousMessages.addAll(msgList);
				
				//call parent function
				for(Message m : msgList)
					processMessage(m);
				
				//sleep
				try {Thread.sleep(delay);} catch(Exception e) {};
			}
		}
	}
	private class SelfDestruct implements Runnable{
		public long ttl;
		public SelfDestruct(long ttl) {this.ttl = ttl;}
		@Override
		public void run() {
			try {Thread.sleep(ttl);} catch(Exception e) {}
			close();
		}
	}
}
