This packages stores all the object prototypes. When retrieving data from databases with MyBatis, one needs to define such a prototype to receive all the columns of a record from a table.

## !important

To pass a standard GeoJSON to backend packaged into `Asset.location` or `Warning.area` (e.g. a controller method that has `@RequestBody Asset asset` as parameter), do use `{locationAsJson: ...}` and  `{areaAsJson: ...}`. See the setters to grasp the idea.

## Asset 

#### type

The type of asset is stored as the type id into database. When querying, one first gets an asset object with the type id, then query again to get the asset type data, which should be performed by the service layer.

#### owner

The owner of the asset (the asset holder) is stored as an id. Since the asset and its holder is not strongly connected, the pojo does not store the owner object.

#### location
The location (drain area) is now confined to be of MultiPolygon type, which is a quadruple nested list. Refer to the test file on how to build such an empty multi-polygon.

## Asset Holder

#### address

Only id is stored within record, data stored in another table. Similar to the above asset type, but won't be packaged into pojo class, but a map.

#### contact preference

Similar to address.

## User

The split of user into user and asset holder is necessary since a user could be both administrator or a non-authorised user.

#### id - username ?

The previous username property is removed. Treat id as username, which might be user as the logging-in username.

#### asset holder

Similarly, only id is stored, and service layer should take responsibility.