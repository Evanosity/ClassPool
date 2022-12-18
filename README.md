# ClassPool
A small utility to reflectively load a set of subclasses.

I created this library to solve the problem of reflection being very slow. How do we turn an object(usually an enum or string) into an equivalent object in an efficient manner?

I found that reflectively loading all of possible classes at once, and then pulling them from the pool as needed, was very efficient. It allows us to avoid enormous switch statements that need grow as the code matures.
