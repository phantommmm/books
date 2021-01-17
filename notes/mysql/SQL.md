     科目     成绩    学号

id subject score stu_id

```
创建学生表
create table student{
id int not null,
subject varchar(255) not null,
score int not null,
stu_id int not null,
primary key('id')
};

查询单科成绩前10的学生     默认升序即第一行成绩最低 desc降序
select * from student order by score desc limit 10;

查询语文科成绩前10的学生
select * from student where subject=chinese order by score desc limit 10

查询总成绩前10的学生
select sum(score) score,stu_id from student group by sut_id order by score desc limit 10

查询平均分前三名的学生
select stu_id, avg(score) score from student group by stu_id order by score desc limit 3

查询语文第1名的学生
select * from student where subject=chinese order by score desc limit 1

查询平均成绩大于80分的同学学号以及平均成绩
select stu_id,avg(score)  average from student group by stu_id having average>80

查询每门成绩都大于80的学号
select stu_id from student where stu_id not in(select distinct stu_id from student where score < 80)

select stu_id from student group by stu_id having min(score)>80

查询各科最高成绩信息
select st.id,st.subject,st.score,st.number from student st inner join (select subject,max(score) max_score from student group by subject) res on st.subject=res.subject and st.score=res.max_score;
```

 

