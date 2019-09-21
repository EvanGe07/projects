/* 1 */
select distinct s.person
from sightings s
where s.location = 'ALasKA FLAT'

/* 2 */
select distinct f.person
from  sightings f, sightings s
where f.location = 'Moreland Mill' and s.location = 'Steve Spring' and s.name = f.name

/* 3 */
select distinct f.genus + ' ' + f.species
from flowers f, sightings s, features fe
where fe.elev > 8250 and f.comname = s.name and fe.location = s.location and (s.person = 'Michael' or s.person = 'Robert')

/* 4 */
select f2.map
from features f2
where exists(
    select s4.location
    from sightings s4
    where s4.location = f2.location and s4.name = 'Alpine penstemon' and month(s4.sighted) = 8
          )

/* 5 */
select distinct f3.genus
from flowers f3
where exists(
    select f4.genus
    from flowers f4
    where f3.genus = f4.genus and f3.species != f4.species
          )

/* 6 */
select count (f4.loc_id)
from features f4
where f4.class = 'Summit' and f4.map = 'Sawmill Mountain'

/* 7 */
create view jamesplace1 as
select *
from features f5
where exists(
    select s5.location
    from sightings s5
    where s5.location = f5.location and s5.person = 'James'
          )

select top(1) a.location
from jamesplace1 a
order by a.latitude

/* 8 */
select distinct s6.person
from sightings s6
where not exists(
    select f6.class
    from features f6, sightings s7
    where f6.location = s7.location and f6.class = 'Tower' and s6.person = s7.person
          )

/* 9 */
create view flower1 as
select s7.person, count (s7.location) as number
from sightings s7
group by s7.person

select fl1.person, fl1.number
from flower1 fl1
where fl1.number = (select max (fl2.number) from flower1 fl2)

/* 10 */
select count(distinct f7.comname)
from flowers f7

create view mysight as
select distinct s8.person, count(distinct s8.name) as number_flowers
from sightings s8
group by s8.person

select max(ms.number_flowers) as maxx
from mysight ms

select s.person, max(s.sighted)
from sightings s, mysight ms
where ms.number_flowers = 50 and s.person = ms.person
group by s.person


/* 11 */
select count(s.sighted)
from sightings s
where s.person = 'Jennifer'

create view Jen as
select count(s.sighted) as month_sights, month(s.sighted) as month
from sightings s
where s.person = 'Jennifer'
group by month(s.sighted)

select 1.0 * j.month_sights/128 , j.month
from Jen j

/* 12 */
drop view JOHN
create view JOHN as
select distinct s.name as name, p.person
from sightings s, PEOPLE p
where p.person = 'John' and s.person = p.person

drop view GUY
create view GUY as
select distinct p.person_id as person_id, s.name as flower_name
from sightings s, PEOPLE p
where s.person = p.person

create view G_count as
select count(g.flower_name) as num, g.person_id as people_id
from GUY g
group by g.person_id

create view INNNER as
select  g.person_id as people_id, j.name as flower_name
from JOHN j
INNER JOIN GUY g on g.flower_name = j.name

create view I_count as
select count(i.flower_name) as num, i.people_id as people_id
from INNNER i
group by i.people_id

create view JI as
select g_c.people_id as people_id, 1.0 * i_c.num / ( g_c.num + 11 - 1.0 * i_c.num) as ji_number
from I_count i_c, G_count g_c
where i_c.people_id = g_c.people_id

select TOP 1 j.ji_number, j.people_id, p.person
from JI j, PEOPLE p
where p.person_id = j.people_id and p.person != 'John'
ORDER by j.ji_number DESC
