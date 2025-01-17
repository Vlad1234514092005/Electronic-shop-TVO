package com.electronic_shop_tvo.electronicshoptvo.repository.jdbc;

import com.electronic_shop_tvo.electronicshoptvo.exception.ItemNotFoundException;
import com.electronic_shop_tvo.electronicshoptvo.exception.QuantityIsUnderZeroException;
import com.electronic_shop_tvo.electronicshoptvo.model.Item;
import com.electronic_shop_tvo.electronicshoptvo.model.dto.RequestQuantity;
import com.electronic_shop_tvo.electronicshoptvo.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JdbcItemRepository implements ItemRepository {

    private static final BeanPropertyRowMapper<Item> ROW_MAPPER = new BeanPropertyRowMapper<>(Item.class);
    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public List<Item> getAllItems() {
        String sqlGetAllItems = """
                SELECT *
                FROM item;
                """;

        return jdbcTemplate.query(sqlGetAllItems, ROW_MAPPER);
    }

    @Override
    public Item getItemById(int id) {
        String sqlGetItemById = """
                SELECT *
                FROM item
                WHERE id = %s;
                """;
        List<Item> itemList = jdbcTemplate.query(sqlGetItemById.formatted(id), ROW_MAPPER);
        if (itemList.isEmpty()) {
            throw new ItemNotFoundException("This list is empty");
        }

        return itemList.get(0);
    }


    @Override
    public List<Item> getItemsByTitle(String title) {
        String sqlGetItemsByTitle = """
                SELECT *
                FROM item
                WHERE title = :title;
                """;

        List<Item> items = jdbcTemplate.query(sqlGetItemsByTitle, Map.of(
                "title", title
        ), ROW_MAPPER);

        if (items.isEmpty()) {
            throw new ItemNotFoundException("This list is empty");
        }

        return items;
    }

    @Override
    public void addNewItem(Item item) {
        String sqlAddItem = """
                INSERT INTO item(title, price, producing_year, manufacturer, quantity, item_type_id)
                VALUES(:title, :price, :producing_year, :manufacturer, :quantity, :item_type_id);
                """;
        jdbcTemplate.update(sqlAddItem, Map.of(
                "title", item.getTitle(),
                "price", item.getPrice(),
                "producing_year", item.getProducingYear(),
                "manufacturer", item.getManufacturer(),
                "quantity", item.getQuantity(),
                "item_type_id", 2
        ));
    }

    @Override
    public void updateItem(int id, Item item) {
        String sqlUpdateItem = """
                UPDATE item
                SET title = :title, price = :price, producing_year = :producing_year, manufacturer = :manufacturer, quantity = :quantity, item_type_id = :item_type_id
                WHERE id = :id;
                """;

        jdbcTemplate.update(sqlUpdateItem, Map.of(
                "id", id,
                "title", item.getTitle(),
                "price", item.getPrice(),
                "producing_year", item.getProducingYear(),
                "manufacturer", item.getManufacturer(),
                "quantity", item.getQuantity(),
                "item_type_id", 2
        ));
    }

    @Override
    public void addQuantity(int id, RequestQuantity requestQuantity) {

        String sqlGetQuantity = """
                SELECT quantity
                FROM item
                WHERE id = %s;
                """;

        Integer currentQuantity = jdbcTemplate.queryForObject(sqlGetQuantity.formatted(id), new HashMap<>(), Integer.class);
        currentQuantity += requestQuantity.quantity();

        String sqlUpdateQuantity = """
                UPDATE item
                SET quantity = :quantity
                WHERE id = :id
                """;

        jdbcTemplate.update(sqlUpdateQuantity, Map.of(
                "id", id,
                "quantity", currentQuantity
        ));
    }

    @Override
    public void removeQuantity(int id, RequestQuantity requestQuantity) {


        String sqlGetQuantity = """
                SELECT quantity
                FROM item
                WHERE id = %s;
                """;

        Integer currentQuantity = jdbcTemplate.queryForObject(sqlGetQuantity.formatted(id), new HashMap<>(), Integer.class);
        currentQuantity -= requestQuantity.quantity();

        if (currentQuantity < 0) {
            throw new QuantityIsUnderZeroException("The quantity is under zero");
        }

        String sqlUpdateQuantity = """
                UPDATE item
                SET quantity = :quantity
                WHERE id = :id
                """;

        jdbcTemplate.update(sqlUpdateQuantity, Map.of(
                "id", id,
                "quantity", currentQuantity
        ));
    }

    @Override
    public void deleteItem(int id) {
        String sqlDeleteItem = """
                DELETE FROM item
                WHERE id = :id          
                """;

        jdbcTemplate.update(sqlDeleteItem, Map.of(
                "id", id
        ));
    }
}
