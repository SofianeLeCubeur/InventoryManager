package com.github.sofiman.inventory.api;

import com.github.sofiman.inventory.api.models.Item;
import org.bson.*;
import org.bson.conversions.Bson;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Query implements Predicate<Item> {

    private byte[] id;
    private String name;
    private boolean exactName;
    private String location;
    private String description;
    private boolean exactDescription;
    private List<Tag> tags;

    private final Map<String, String> simpleFields = new HashMap<>();

    public final Query withId(byte[] id){
        this.id = id;

        return this;
    }

    public final Query withName(String name, boolean exact){
        this.name = name;
        this.exactName = exact;

        return this;
    }

    public final Query withName(String name){
        return withName(name, true);
    }

    public final Query with(String field, String value){
        this.simpleFields.put(field, value);

        return this;
    }

    public final Query withLocation(String location){
        this.location = location;

        return this;
    }

    public final Query withDescription(String description, boolean exact){
        this.description = description;
        this.exactDescription = exact;

        return this;
    }

    public final Query withDescription(String description){
        return withDescription(description, true);
    }

    public final Query withTag(Tag tag){
        if(this.tags == null){
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);

        return this;
    }

    public final Query withTags(Tag... tags){
        if(this.tags == null){
            this.tags = Arrays.asList(tags);
        } else {
            this.tags.addAll(Arrays.asList(tags));
        }

        return this;
    }

    public boolean test(Item item){
        if(this.id != null){
            if(item.getId() != this.id){
                return false;
            }
        }
        if(this.name != null){
            if(this.exactName){
                if(!item.getName().equals(this.name)){
                    return false;
                }
            } else {
                if(!item.getName().toLowerCase().contains(this.name.toLowerCase())){
                    return false;
                }
            }
        }
        if(this.location != null){
            boolean contains = false;
            for(LocationPoint p : item.getLocationHistory()){
                if(p.getLocation().equalsIgnoreCase(this.location)){
                    contains = true;
                }
            }
            if(!contains){
                return false;
            }
        }
        if(this.description != null){
            if(this.exactDescription){
                if(!item.getDescription().equals(this.description)){
                    return false;
                }
            } else {
                if(!item.getDescription().toLowerCase().contains(this.description.toLowerCase())){
                    return false;
                }
            }
        }
        if(this.tags != null){
            if(!item.getTags().containsAll(this.tags)){
                return false;
            }
        }

        for(Map.Entry<String, String> field : simpleFields.entrySet()){
            String key = field.getKey();
            String value = field.getValue();
            String getter = "get" + key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
            int idx = -1;
            while((idx=getter.indexOf("_")) > 0){
                getter = getter.substring(0, idx) + getter.substring(idx+1, idx+2).toUpperCase() + getter.substring(idx+2).toLowerCase();
            }

            try {
                Method m = item.getClass().getDeclaredMethod(getter);
                m.setAccessible(true);
                Object result = m.invoke(item);
                if(result != null){
                    if(!value.equals(result.toString())){
                        return false;
                    }
                }
            } catch (Exception e){
                System.err.println("[Query] Warning: Field " + key + " ignored in testing: " + e.getMessage());
            }
        }

        return true;
    }

    public Bson toBson(){
        Document d = new Document();
        if(this.id != null){
            d.append("_id", this.id);
        }
        if(this.name != null){
            if(this.exactName){
                d.append("name", this.name);
            } else {
                Document filter = new Document();
                filter.append("$regex", Pattern.quote(this.name));
                filter.append("$options", "i");
                d.append("name", filter);
            }
        }
        for(Map.Entry<String, String> field : simpleFields.entrySet()){
            d.append(field.getKey(), field.getValue());
        }
        if(this.location != null){
            // For objects which doesn't have a location history
            d.append("location", this.location);
            // For objects that does have a location history
            // The template is { locations: { $in: [ { location: "your_location" } ] }
            Document filter = new Document();
            BsonArray locationFilter = new BsonArray();
            BsonDocument locationEntry = new BsonDocument();
            locationEntry.append("location", new BsonString(this.location));
            locationFilter.add(locationEntry);
            filter.append("$in", locationFilter);
            d.append("locations", filter);
        }
        if(this.description != null){
            if(this.exactDescription){
                d.append("description", this.description);
            } else {
                // The template is { description: { $regex: "descriptiontomatch", $options: "i" } }
                Document filter = new Document();
                filter.append("$regex", Pattern.quote(this.description));
                filter.append("$options", "i");
                d.append("description", filter);
            }
        }
        if(this.tags != null && this.tags.size() > 0){
            // The template is { tags: { $in: [ { id: "tag_id" } ] }
            Document filter = new Document();
            BsonArray array = new BsonArray();
            BsonDocument tagFilter;
            for(Tag tag : this.tags){
                tagFilter = new BsonDocument();
                tagFilter.append("id", new BsonString(tag.getId()));
                array.add(tagFilter);
            }
            filter.append("$in", array);
            d.append("tags", filter);
        }

        return d;
    }
}
