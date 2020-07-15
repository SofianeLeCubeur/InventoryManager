class Inventory {

    constructor(id, name){
        this._id = id;
        this._name = name;
        this._locationHistory = [];
    }

    get id(){
        return this.id;
    }

    get name(){
        return this._name;
    }

    get state(){
        return this._state;
    }

    set state(state){
        this._state = state;
    }

    set location(location){
        this._locationHistory = [location, ...this._locationHistory];
    }

    get location(){
        return this._locationHistory[0];
    }

}

module.exports = {

};