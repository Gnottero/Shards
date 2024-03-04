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
                'maxCap' -> 8,
                'storedInvs' -> [],
                'Enchantments' -> [{}],
                'display' -> {
                    'Name' -> encode_nbt('{"text": "Storage Tome", "color": "white", "italic": false}')
                }
            }),
            spawn('item', pos(_), {
                'Item' -> {
                    'id' -> 'clock',
                    'Count' -> 1,
                    'tag' -> {
                        'isStorageTome' -> true,
                        'maxCap' -> 8,
                        'storedInvs' -> [],
                        'Enchantments' -> [{}],
                        'display' -> {
                            'Name' -> encode_nbt('{"text": "Storage Tome", "color": "white", "italic": false}')
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
global_invalid_blocks = ['blast_furnace', 'furnace', 'smoker', 'brewing_stand', 'hopper'];

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

create_inventory_panel(player, book_data) -> (
    try(
        (
            in_dimension(book_data:'storedInvs':(book_data:'Page'):'dimension',
                block_pos = book_data:'storedInvs':(book_data:'Page'):'pos';
                print(player, block(block_pos));
                inv_screen = create_screen(player, 'generic_9x4', 'Storage Controller', _(screen, player, action, data) -> (
                    
                    if (['pickup', 'pickup_all', 'swap', 'quick_move', 'throw'] ~action != null && [range(27, 36)] ~(data: 'slot') != null,
                        return('cancel');
                    );

                ));

                task(_(outer(inv_screen)) -> (
                    for(range(27, 36),
                        if ([27, 31, 35] ~ _ == null,
                            inventory_set(inv_screen, _, 1, 'gray_stained_glass_pane');
                        );
                        if (_ == 31,
                            inventory_set(inv_screen, _, 1, 'paper', {
                                'display' -> {
                                    'Name' -> encode_nbt('{"text": "Search Item", "color": "white", "italic": false}');
                                }
                            });
                        );

                        if (_ == 27,
                            inventory_set(inv_screen, _, 1, 'player_head', {
                                'SkullOwner' -> 'MHF_ArrowLeft',
                                'display' -> {
                                    'Name' -> encode_nbt('{"text": "Previous Page", "color": "white", "italic": false}');
                                }
                            });
                        );

                        if (_ == 35,
                            inventory_set(inv_screen, _, 1, 'player_head', {
                                'SkullOwner' -> 'MHF_ArrowRight',
                                'display' -> {
                                    'Name' -> encode_nbt('{"text": "Next Page", "color": "white", "italic": false}');
                                }
                            });
                        );
                    );
                ));

            );
        ),
        'exception',
        (
            print(player, 'An error occurred while scanning the inventories. Please make sure that they exist');
        )
    )
);

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

    if (item_tuple:0 == 'clock' && item_tuple:2:'isStorageTome',
        nbt = parse_nbt(item_tuple:2);

        if (is_storage_controller([query(player, 'dimension'), ...pos(block)]),
            if (block_data(pos(block)) ~ 'Book' == null,
                states = block_state(block);
                states:'has_book' = true;
                if (nbt ~ 'Page' ~ null, nbt:'Page' = 0);
                set(pos(block), block, states, {'Book' -> {'id' -> 'clock', 'Count' -> 1, 'tag' -> encode_nbt(nbt)}});
                inventory_set(player, query(player, 'selected_slot'), item_tuple:1 - 1, item_tuple:0, encode_nbt(nbt));
                return('cancel');
            );
        );

        if (inventory_has_items(block) != null && query(player, 'sneaking') && global_invalid_blocks ~ block == null,            
            block_info = {'dimension' -> query(player, 'dimension'), 'pos' -> pos(block)};

            // Remove the inventory from the list if the player is clicking on a storage block that was already saved

            if (nbt:'storedInvs' ~ block_info != null, 
                (
                    print(player, 'Inventory removed from the inventory list');
                    delete(nbt: 'storedInvs', nbt: 'storedInvs'~block_info);
                    nbt:'display':'Lore' = [
                        str('{"text": "Linked Inventories: %d/%d", "color": "gray", "italic": false}', length(nbt:'storedInvs'), nbt:'maxCap')
                    ];
                    inventory_set(player, query(player, 'selected_slot'), item_tuple:1, item_tuple:0, encode_nbt(nbt));
                    return();
                )
            );

            // Add a new storage block if the amount of stored inventories is below max capacity

            if (length(nbt:'storedInvs') < nbt:'maxCap',
                nbt:'storedInvs' += block_info;
                nbt:'display':'Lore' = [
                    str('{"text": "Linked Inventories: %d/%d", "color": "gray", "italic": false}', length(nbt:'storedInvs'), nbt:'maxCap')
                ];
                inventory_set(player, query(player, 'selected_slot'), item_tuple:1, item_tuple:0, encode_nbt(nbt));
                particle('happy_villager', pos(block) + [0.5, 0.5, 0.5], 10, 0.6, 0.6, player);
                print(player, 'Inventory stored successfully');
                return();
            );
        );
    );

    if (is_storage_controller([query(player, 'dimension'), ...pos(block)]),
    
        if (block_data(pos(block)) ~ 'Book' == null, return('cancel'));
        if (!(block_data(pos(block)):'Book.tag.isStorageTome'), return());

        if (!query(player, 'sneaking'),
            (
                book_data = parse_nbt(block_data(pos(block)):'Book.tag');
                create_inventory_panel(player, book_data);
                return('cancel');
            ),
            (
                states = block_state(block);
                states:'has_book' = false;
                book_data = parse_nbt(block_data(pos(block)):'Book');
                set(pos(block), block, states, {});
                spawn('item', pos(player), {
                    'Item' -> {
                        'id' -> replace(book_data:'id', 'minecraft:'),
                        'Count' -> book_data:'Count',
                        'tag' -> encode_nbt(book_data:'tag')
                    }
                });
                return();
            )
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
