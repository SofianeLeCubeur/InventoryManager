FROM node:14

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
# where available (npm@5+)
COPY package*.json ./

RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Bundle app source
COPY . .

# Open the web port
EXPOSE 8080
# Open the Redis port
EXPOSE 6379
# Open the MongoDB port
EXPOSE 27017
CMD [ "npm", "run", "serve" ]