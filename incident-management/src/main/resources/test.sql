-----------------------------------------------------------------------------------
Table - users:

id	userid	firstname	lastname	department	role	email	          password
1	1001	fn	         ln	        SSD	        admin	admin@gmail.com	  Password
-----------------------------------------------------------------------------------
Table - role

id	   name	            created_by
1	   Administrator	1
2	   LDAP_USER	    1
-----------------------------------------------------------------------------------
Table - user_role_mapping

id	  user_id 	role_id
1	  1	        1
-----------------------------------------------------------------------------------
Table - feature

id	  name	                  path
1	  Dashboard	             /dashboard
2	  Activity Report	     /activityReport
3	  Report Manager	     /reportManager
4	  User Management	     /userManagement
5	  Role Management	     /roleManagement
6	  PA Management	         /paManagement
7	  PA Group Management	 /groupManagement
8	  PA Zone Management	 /zoneManagement
9	  PA Management          /paScheduleManagement
10	  Public Announcement 	 /publicAnnouncementReport
-----------------------------------------------------------------------------------
Table - role_feature_mapping

id	  role_id	feature_id
1	  1	        1
2	  1	        2
3	  1	        3
4	  1	        4
5	  1	        5
-----------------------------------------------------------------------------------
