__config() -> {
    'stay_loaded' -> true,

    'commands' -> {
        'give <target>' -> 'give_scale_device'
    },
    'arguments' -> {
        'target' -> {
            'type' -> 'entities',
            'players' -> true
        }
    }
};

// ----[Utility]----
global_delta_scale = 0.25;

give_scale_device(targets) -> (
    for (targets,
        empty_slot = inventory_find(_, null);
        if (empty_slot != null && ([36, 37, 38, 39, 40] ~empty_slot) == null,
            inventory_set(_, empty_slot, 1, 'clock', {
                'isScaleDevice' -> true,
                'Enchantments' -> [{}],
                'display' -> {
                    'Name' -> encode_nbt('{"text": "Shrinking Device", "color": "white", "italic": false}')
                }
            }),
            spawn('item', pos(_), {
                'Item' -> {
                    'id' -> 'clock',
                    'Count' -> 1,
                    'tag' -> {
                        'isScaleDevice' -> true,
                        'Enchantments' -> [{}],
                        'display' -> {
                            'Name' -> encode_nbt('{"text": "Shrinking Device", "color": "white", "italic": false}')
                        }
                    }
                }
            })
        )
    );
);

change_scale(target, dscale) -> (
    tscale = query(target, 'attribute', 'generic.scale');
    if (tscale + dscale > 0,
        run(str('attribute %s minecraft:generic.scale base set %f', query(target, 'command_name'), tscale + dscale)),
        print(target, '[Shrinking Device]#> You are not allowed to shrink further!')
    )
);

set_skull(screen, player) -> (
    inventory_set(screen, 13, 1, 'player_head', {
        'SkullOwner' -> player,
        'display' -> {
            'Name' -> encode_nbt(str('{"text": "Current Scale: %.2f", "color": "white", "italic": false}', query(player, 'attribute', 'generic.scale')));
        }
    });
);
// -----------------

// ----[Events]----
__on_player_uses_item(player, item_tuple, hand) -> (

    // Return if hand is not mainhand
    if (hand != 'mainhand',
        return ());

    // Return if item is not a clock
    if (item_tuple: 0 != 'clock',
        return ());

    // Return if item does not have the isScaleDevice tag
    if (!item_tuple: 2: 'isScaleDevice',
        return ());

    device_screen = create_screen(player, 'generic_9x3', 'Shriking Device', _(screen, player, action, data) -> (
        if (['pickup', 'pickup_all', 'swap', 'quick_move', 'throw'] ~action != null && [range(27)] ~(data: 'slot') != null,
            if ([11, 13, 15] ~(data: 'slot') != null,

                slot = data: 'slot';

                if (slot == 11, change_scale(player, -global_delta_scale));
                if (slot == 13, change_scale(player, 1 - query(player, 'attribute', 'generic.scale')));
                if (slot == 15, change_scale(player, global_delta_scale));

                set_skull(screen, player);

            );
            'cancel'
        );
    ););

    task(_(outer(device_screen), outer(player)) -> (
        loop(27,
            if ([11, 13, 15] ~_ == null,
                (
                    inventory_set(device_screen, _, 1, 'gray_stained_glass_pane', {
                        'placeholder' -> true
                    });
                )
            );

            if (_ == 11,
                inventory_set(device_screen, 11, 1, 'player_head', {
                    'SkullOwner' -> 'MHF_ArrowDown',
                    'display' -> {
                        'Name' -> encode_nbt(str('{"text": "Decrease by %.2f", "color": "white", "italic": false}', global_delta_scale));
                    }
                });
            );

            if (_ == 13,
                set_skull(device_screen, player);
            );

            if (_ == 15,
                inventory_set(device_screen, 15, 1, 'player_head', {
                    'SkullOwner' -> 'MHF_ArrowUp',
                    'display' -> {
                        'Name' -> encode_nbt(str('{"text": "Increase by %.2f", "color": "white", "italic": false}', global_delta_scale));
                    }
                });
            );
        );
    ););

);
// ----------------