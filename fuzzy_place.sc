__config() -> {

    // Keep the script loaded through world loads
    'stay_loaded' -> true,

    // Define a utility command
    'commands' -> {
        'place <ipos> <epos> <blocks>' -> 'fuzzy_place'
    },
    'arguments' -> {
        'ipos' -> {'type' -> 'pos'},
        'epos' -> {'type' -> 'pos'},
        'blocks' -> {'type' -> 'string'}
    }
};


fuzzy_place(ipos, epos, palette) -> (
    blocks = split(',', replace(palette, '\\s+', ''));
    volume(ipos, epos,
        set(_, blocks:rand(length(blocks)));
    );
)
