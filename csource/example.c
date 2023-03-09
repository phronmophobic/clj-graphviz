#include "gvc.h"
#include "cgraph.h"

int main(){

    printf("directed: %d\n", Agdirected.directed );
    printf("strict: %d\n", Agdirected.strict );
    printf("no_loop: %d\n", Agdirected.no_loop );
    printf("maingrap: %d\n", Agdirected.maingraph);
    printf("flatlock: %d\n", Agdirected.flatlock );
    printf("no_write: %d\n", Agdirected.no_write );
    printf("has_attrs: %d\n", Agdirected.has_attrs );
    printf("has_cmpn: %d\n", Agdirected.has_cmpnd);

    Agraph_t *g;
    g = agopen("G", Agdirected, NULL);

    printf("size: %d\n", sizeof(Agraph_t));

    return 0;

}

