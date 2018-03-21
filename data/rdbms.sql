CREATE TABLE "dept" (
      "deptno" INTEGER UNIQUE,
      "dname" VARCHAR(30),
      "loc" VARCHAR(100));
INSERT INTO "dept" ("deptno", "dname", "loc")
       VALUES (10, 'APPSERVER', 'NEW YORK');

CREATE TABLE "emp" (
      "empno" INTEGER PRIMARY KEY,
      "ename" VARCHAR(100),
      "job" VARCHAR(30),
      "deptno" INTEGER REFERENCES "dept" ("deptno"),
      "etype" VARCHAR(30));
INSERT INTO "emp" ("empno", "ename", "job", "deptno", "etype" )
       VALUES (7369, 'SMITH', 'CLERK', 10, 'PART_TIME');
