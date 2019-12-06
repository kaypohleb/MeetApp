package com.example.meetapp.utility;

import android.util.Log;

import com.example.meetapp.ui.IncomingFragment;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Polls {
    private static ArrayList<PollDetails> pollVoteList = new ArrayList<>();
    private static ArrayList<Poll> allPolls = new ArrayList<>();
    private static Poll selectedPoll;

    public static ArrayList<Poll> getAllPolls() {
        return allPolls;
    }

    public static void setPollDetailsList(ArrayList<PollDetails> pollDetailsList) {
        Polls.pollVoteList = pollDetailsList;
    }
    public static void removePollVoteList(PollDetails pollDetails){
        pollVoteList.remove(pollDetails);
    }
    public static void addPollVoteList(PollDetails pollDetails){
        pollVoteList.add(pollDetails);
    }
    public static ArrayList<PollDetails> getPollVoteList() {
        return pollVoteList;
    }

    public static void setAllPolls(String s) {
        Polls.allPolls = parseJSON(s);
    }

    public static Poll getSelectedPoll() {
        return selectedPoll;
    }

    public static void setSelectedPoll(Poll selectedPoll) {
        Polls.selectedPoll = selectedPoll;
        allPolls.get(allPolls.indexOf(selectedPoll)).setSelected(true);

    }

    private static ArrayList<Poll> parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Poll>>(){}.getType();
        ArrayList<Poll> pollList = gson.fromJson(jsonString, type);
        for (Poll poll : pollList){
            Log.i("Organized Details", poll.getDate() + "-" + poll.getTotal_vote());
        }
        return pollList;
    }

    public class Poll{
        private String date_id;
        private String event_id;
        private String date;
        private String total_vote;
        private Boolean selected = false;

        public String getEvent_id() {
            return event_id;
        }

        public String getTotal_vote() {
            return total_vote;
        }

        public String getDate_id() {
            return date_id;
        }

        public String getDate() {
            return date;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }
    }

}
