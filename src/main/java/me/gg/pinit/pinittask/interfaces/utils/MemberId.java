package me.gg.pinit.pinittask.interfaces.utils;

/**
 * 멤버 ID 를 나타내는 어노테이션이다.
 * 추후 멤버 인증 과정에서 해당 멤버 ID를 추출하여 ArgumentResolver 등에서 활용할 수 있다.
 * 현재는 미구현이지만, 실제 인증 로직이 추가되면 이 어노테이션을 통해 멤버 ID를 주입받을 수 있다.
 */
public @interface MemberId {
}
