package br.com.atm.terminal.service

class MessageEvent {

    /*
        SUBSCRIBE(1),
       UNSUBSCRIBE(2),
       FUNCTION(3),
       MESSAGE(4)
     */

    var type = 0
    var topic = ""
    var text = ""
    var cls = ""
    override fun toString(): String {
        return "MessageEvent(type=$type, topic='$topic', text='$text', cls='$cls')"
    }


}