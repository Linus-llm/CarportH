-- find wood by category and needed length
SELECT woods.id, woods.profile_id, price, width, height, length FROM woods
JOIN wood_profiles ON woods.profile_id=wood_profiles.id
WHERE length-3100 >= 0 AND category=0
ORDER BY length-3100 ASC LIMIT 1

