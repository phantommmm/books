http://sqlfiddle.com/ 在线sql，左边创建表，右边crud



     科目     成绩    学号

id subject score stu_id

```
创建学生表
create table student(
id int not null,
subject varchar(255) not null,
score int not null,
stu_id int not null,
primary key(id)
);

插入数据
insert into student values(1,"math",50,1);
insert into student values(2,"pe",80,1);
insert into student values(3,"math",90,2);
insert into student values(4,"pe",80,2);
insert into student values(5,"math",30,3);
insert into student values(6,"pe",80,3);


查询单科成绩前10的学生     默认升序即第一行成绩最低 desc降序
select * from student order by score desc limit 10;

查询语文科成绩前10的学生
select * from student where subject=chinese order by score desc limit 10

查询总成绩前10的学生
select sum(score) sum_score,stu_id from student group by stu_id order by sum_score desc limit 10

查询平均分前三名的学生
select stu_id, avg(score) avg_score from student group by stu_id order by avg_score desc limit 3

查询语文第1名的学生
select * from student where subject=chinese order by score desc limit 1

查询平均成绩大于80分的同学学号以及平均成绩
select stu_id,avg(score)  average from student group by stu_id having average>80

查询每门成绩都大于80的学号
select stu_id from student where stu_id not in(select distinct stu_id from student where score < 80)

select stu_id from student group by stu_id having min(score)>80

查询各科最高成绩分数
select subject,max(score) max_score from student group by subject;


查询各科最高成绩分数以及对应的学生id 同科最高成绩可能有多个
select st.subject,st.score,st.stu_id from student st inner join (select subject,max(score) max_score from student group by subject) res on st.subject=res.subject and st.score=res.max_score;

查询各科最高成绩分数以及对应的学生id 同科最高成绩只要一个就可
select st.subject,st.score,st.stu_id from student st inner join (select subject,max(score) max_score from student group by subject) res on st.subject=res.subject and st.score=res.max_score group by st.subject;

select subject,distinct stu_id from student
会报错 distinct必须放在开头
select distinct subject,stu_id from student
此时distinct会共同作用与subject和stu_id，只有2个都相同才会被排除
```

 

```
统计数据表time中每个小时记录数
id	p_date
1	2020/08/29 00:10:10
2	2020/08/29 01:10:10
3	2020/08/29 01:10:10
4	2020/08/29 02:10:10
5	2020/08/29 01:10:10


select Hour(p_date),COUNT(*) from time  group by HOUR(p_date) order by HOUR(p_date)	
```



```
给定一个表T，表结构如下: id, name, salary, city 
根据两个条件查找name 
1. salary 大于等于 10000 
2. 所在 city 的平均 salary 大于等于 5000

select t.name from t where t.salary in (select salary from t where salary > 10000) group by city having avg(t.salary) >=5000
```

