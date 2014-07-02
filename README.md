Bitcask简洁、优雅的key/value存储引擎
==========
在关系数据库存储上，Btree一直是主角，但在某些情况下，log(n)的读写操作并不是总是让人满意。 Bitcask是一种连续写入很快速的Key/Value数据存储结构，读写操作的时间复杂度均为常量。

####BitCask连续写入操作
Bitcask具有高效的连续写入操作，连续写操作类似向log文件追加记录，因此Bitcask也被称作是日志结构存储。

BitCash将存储对象的key、value分别存储：
*   在内存中对key创建索引
*   磁盘文件存储value数据

当有数据需要写入时，磁盘无需遍历文件，直接写入到数据块或者文件的末尾，避免了磁盘机械查找的时间，写入磁盘之后，只需要在内存的HashMap中更新相应的索引,内存中用HashMap来保存一条记录的索引部分，一条索引包含的信息如下：

*   [Key: Jason, Filename: employee.db, Offset:0, Size:146, ModifiedDate:2343432312]

*   [Key: Bill, Filename: employee.db, Offset:146, Size:146, ModifiedDate:5489354345]

Key表示一条记录的主键，查找通过它在HashMap中找到完整索引信息
Filename是磁盘文件名字，通过它和Offset找到Value在磁盘的开始位置
Offset是Value在文件中偏移量，通过它和Size可以读取一条记录
Size是Value所占的磁盘大小，单位是Byte
假设目前数据库中已有上述两条的记录，当我要写入key为 "Jobs"， value为: object的一条新记录时， 只需要在文件employee.db的末尾写入value=object，在HashMap中添加索引：[Key: Jobs, Filename: employee.db, Offset:292, Size:146, ModifiedDate:9489354343] 即可。

最后数据库就包含了三条索引信息：

*   [Key: Jason, Filename: employee.db, Offset:0, Size:146, ModifiedDate:2343432312]
*   [Key: Bill, Filename: employee.db, Offset:146, Size:150, ModifiedDate:5489354345]
*   [Key: Jobs, Filename: employee.db, Offset:294, Size:136, ModifiedDate:948965443] 

####BitCask随机读取操作

由于数据在内存当中使用HashMap作为索引，查找索引的时间为常量，比如查找Bill，直接通过Key就可以找到它的索引信息，再根据索引信息，找到value在文件位置和大小，精确读取出bytes，反序列化成value对象。 当然在value存入文件时需要序列化内存对象成bytes。磁盘读取的过程的时间复杂度也是常量， 并不会随时数据的增大而增大。

####BitCask 数据删除和更新
一条记录包含了索引和数据两个部分,删除索引容易，但要彻底的删除数据不是件容易的事情（不讨论，参考磁盘空间整理）。对于更新数据，Bitcash通常采用的策略是append一条新数据，并更新已有的索引，至于旧数据则在清理数据的时候把它删除掉。  

####BitCask适合的场景
*   适合连续写入，随机的读取，连续读取性能不如Btree；
*   记录的key可以完全的载入内存；
*   value的大小比key大很多,否则意义不大；
