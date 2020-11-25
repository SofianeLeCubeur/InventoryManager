module.exports = {
    Message(message, initiator){
        return { success: true, message, initiator };
    },
    User(user){
        let cutUser = {...user, success: true};
        delete cutUser.password;
        cutUseruser.id = cutUser._id;
        delete cutUser._id;
        return cutUser;
    },
    Content(content){
        let obj = {...content, success: true };
        obj.id = obj._id;
        delete obj._id;
        return obj;
    },
    Inventory(inv){
        return { success: true, id: inv._id, name: inv.name, icon: inv.icon, location: inv.location, state: inv.state, items: inv.items };
    },
    Container(cnt){
        let location = cnt.locations.length > 0 && cnt.locations.sort((a, b) => b-a)[0].location || '';
        return { success: true, id: cnt._id, content: cnt.content, location, state: cnt.state }
    },
    Item(it){
        return { success: true, id: it._id, name: it.name, description: it.description, reference: it.reference, serial_number: it.serial_number, icon: it.icon };
    },
    SmallItem(it){
        return { success: true, id: it._id, name: it.name, description: it.description, icon: it.icon };
    },
    Token(token){
        return { success: true, token: token.token, type: 'Bearer', scope: token.scope };
    },
    Error(type, message){
        return { success: false, err: type, err_description: message };
    }
}