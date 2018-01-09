package com.scaledrone.lib;

import java.util.ArrayList;

public interface ObservableRoomListener {
    void onMembers(Room room, ArrayList<Member> members);
    void onMemberJoin(Room room, Member member);
    void onMemberLeave(Room room, Member member);
}
