module.exports = {
    escapeRegExp(s){
        return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
};