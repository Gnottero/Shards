__config() -> {
    'stay_loaded' -> true,
    'scope' -> 'global',

    'commands' -> {
        'give_controller <target>' -> 'give_storage_controller',
        'give_tome <target>' -> 'give_storage_tome'
    },
    'arguments' -> {
        'target' -> {
            'type' -> 'entities',
            'players' -> true
        }
    }
};

// ----[Command Function]----
give_storage_controller(targets) -> (
    for (targets,
        empty_slot = inventory_find(_, null);
        if (empty_slot != null && ([36, 37, 38, 39, 40] ~empty_slot) == null,
            inventory_set(_, empty_slot, 1, 'lectern', {
                'isStorageController' -> true,
                'Enchantments' -> [{}],
                'display' -> {
                    'Name' -> encode_nbt('{"text": "Storage Controller", "color": "white", "italic": false}');
                }
            }),
            spawn('item', pos(_), {
                'Item' -> {
                    'id' -> 'lectern',
                    'Count' -> 1,
                    'tag' -> {
                        'isStorageController' -> true,
                        'Enchantments' -> [{}],
                        'display' -> {
                            'Name' -> encode_nbt('{"text": "Storage Controller", "color": "white", "italic": false}');
                        }
                    }
                }
            })
        )
    );
);

give_storage_tome(targets) -> (
    for (targets,
        empty_slot = inventory_find(_, null);
        if (empty_slot != null && ([36, 37, 38, 39, 40] ~empty_slot) == null,
            inventory_set(_, empty_slot, 1, 'clock', {
                'isStorageTome' -> true,
                'storedInvs' -> [],
                'Enchantments' -> [{}],
                'display' -> {
                    'Name' -> encode_nbt('{"text": "Storage Tome", "color": "white", "italic": false}'),
                    'Lore' -> encode_nbt(['{"text": "Linked Inventories: 0/8", "color": "gray", "italic": false}'])
                }
            }),
            spawn('item', pos(_), {
                'Item' -> {
                    'id' -> 'clock',
                    'Count' -> 1,
                    'tag' -> {
                        'isStorageTome' -> true,
                        'storedInvs' -> [],
                        'Enchantments' -> [{}],
                        'display' -> {
                            'Name' -> encode_nbt('{"text": "Storage Tome", "color": "white", "italic": false}'),
                            'Lore' -> encode_nbt(['{"text": "Linked Inventories: 0/8", "color": "gray", "italic": false}'])
                        }
                    }
                }
            })
        )
    );
);
// ----------------


// ----[Utility]----
global_file = null;

get_storage_loc() -> (
    if (list_files('', 'json') ~'controllers' != null,
        global_file = read_file('controllers', 'json');
    );
);

remove_storage_from_list(block_info) -> (
    if (global_file: 'controllers'~block_info == null,
        return ()); delete(global_file: 'controllers', global_file: 'controllers'~block_info);

    // Drop a storage controller
    schedule(0, _(outer(block_info)) -> (
        lectern = first(entity_area('item', [block_info: 1, block_info: 2, block_info: 3] + [0.5, 0.5, 0.5], 0.2, 0.2, 0.2), query(_, 'nbt'): 'Item': 'id' == 'minecraft:lectern');
        if (lectern != null,
            modify(lectern, 'nbt_merge', {
                'Item' -> {
                    'tag' -> {
                        'isStorageController' -> true,
                        'Enchantments' -> [{}],
                        'display' -> {
                            'Name' -> encode_nbt('{"text": "Storage Controller", "color": "white", "italic": false}');
                        }
                    }
                }
            });
        );
    ););
);

is_storage_controller(block_info) ->
    return (global_file: 'controllers'~block_info != null);

// ----------------

// ----[Events]----
__on_start() -> (
    if (list_files('', 'json') ~'controllers' == null,
        write_file('controllers', 'json', {
            'controllers' -> []
        });
    ); get_storage_loc()
);

__on_close() -> (
    if (list_files('', 'json') ~'controllers' != null,
        write_file('controllers', 'json', global_file),
        print(player('all'), 'An error occurred while trying to save the storage controllers locations!');
    );
);


// Handle the placing of the Storage Controller
__on_player_places_block(player, item_tuple, hand, block) -> (
    if (block != 'lectern',
        return ());
    if (!item_tuple: 2: 'isStorageController',
        return ());

    global_file: 'controllers' += [query(player, 'dimension'), ...pos(block)];
);

// Link a Storage Controller to the slave inventories
// Needs some fixes

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (

    if (hand != 'mainhand',
        return ());

    if (item_tuple:0 == 'clock' && item_tuple:2:'isStorageTome' && query(player, 'sneaking'),
        nbt = parse_nbt(item_tuple:2);

        if (is_storage_controller([query(player, 'dimension'), ...pos(block)]),
            if (block_data(pos(block)) ~ '' == null,
                print(player, 'Empty Storage');
                set(block, block, block_state(block), {''})
            );
        );

        if (inventory_has_items(block) != null && length(nbt:'storedInvs') < 8,            
            block_info = {'dimension' -> query(player, 'dimension'), 'pos' -> pos(block)};
            if (nbt:'storedInvs' ~ block_info != null,
                (
                    print(player, 'Inventory already saved in the Storage Tome');
                    return();
                ),
                (
                    nbt:'storedInvs' += block_info;
                    nbt:'display':'Lore' = [
                        str('{"text": "Linked Inventories: %d/8", "color": "gray", "italic": false}', length(nbt:'storedInvs'))
                    ];
                    inventory_set(player, query(player, 'selected_slot'), item_tuple:1, item_tuple:0, encode_nbt(nbt));
                    particle('happy_villager', pos(block) + [0.5, 0.5, 0.5], 10, 0.6, 0.6, player);
                    print(player, 'Inventory stored successfully');
                    return();
                )
            );
        );
    );



);



// Handle the breaking of the Storage Controller
__on_player_breaks_block(player, block) -> (
    if (block == 'lectern',
        remove_storage_from_list([query(player, 'dimension'), ...pos(block)]);
    );
);

__on_explosion_outcome(pos, power, source, causer, mode, fire, blocks, entities) -> (
    for (blocks,
        if (_ == 'lectern',
            remove_storage_from_list([query(source, 'dimension'), ...pos(_)]);
        );
    );
);
// ----------------